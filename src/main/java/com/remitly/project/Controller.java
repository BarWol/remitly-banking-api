package com.remitly.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.lang.runtime.ObjectMethods;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/swift-codes")
public class Controller {

    private final LocalDatabase localdb;

    @Autowired
    public Controller(LocalDatabase localdatabase) {
        this.localdb = localdatabase;
    }


    @GetMapping("/test")
    public String getAllSwiftCodes() {
        String sql = "SELECT * FROM banks";
        StringBuilder result = new StringBuilder();
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.append(rs.getString("name")).append(" ").append(rs.getString("swift_code")).append("\n");
            }
            return result.length() > 0 ? result.toString() : "No SWIFT codes found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving SWIFT codes: " + e.getMessage();
        }
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<String> getSwiftCode(@PathVariable String swiftCode) {
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        // Customize the pretty printer with 4-space indentation
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(new DefaultIndenter("    ", "\n")); // 4 spaces for arrays
        prettyPrinter.indentObjectsWith(new DefaultIndenter("    ", "\n")); // 4 spaces for objects

        if (isHeadquarter(swiftCode)) {
            List<Branch> branches = getBranches(swiftCode);
            Hq hq = getHeadquarter(swiftCode, branches);
            try {
                if (hq == null) {
                    return new ResponseEntity<>("message: headquarters with this swiftCode does not exist", HttpStatus.NOT_FOUND);
                }
                return ResponseEntity.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hq));
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("message: failed to retrieve headquarters, sql exception", HttpStatus.BAD_REQUEST);
            }
        } else {
            Branch branch = getBranch(swiftCode);
            try {
                if(branch == null)
                {
                    return new ResponseEntity<>("message: branch with this swiftCode does not exist", HttpStatus.NOT_FOUND);
                }
                return ResponseEntity.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(branch));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }

    }
    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<String> getSwiftCodesByCountry(@PathVariable String countryISO2code) {
        String branches_sql = "SELECT address, bankName, countryISO2, isHeadquarter, swiftCode FROM branches WHERE countryISO2 = ? "+
                "UNION "+
                "SELECT address, bankName, countryISO2, isHeadquarter, swiftCode FROM headquarters WHERE countryISO2 = ?";
        String countryNameSql = "SELECT countryName FROM headquarters WHERE countryISO2 = ?";
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(branches_sql))
        {
            stmt.setString(1,countryISO2code);
            stmt.setString(2,countryISO2code);
            ResultSet rs = stmt.executeQuery();
            PreparedStatement stmt2 = localdb.getConnection().prepareStatement(countryNameSql);
            stmt2.setString(1,countryISO2code);
            ResultSet rs2 = stmt2.executeQuery();

            if(!rs2.next())
            {
                return new ResponseEntity<>("message: country with this ISO2 code does not exist", HttpStatus.NOT_FOUND);

            }
            String countryName = rs2.getString("countryName");
            List<Branch> branches = new ArrayList<Branch>();
            while (rs.next()) {
                branches.add(
                        new Branch
                                (rs.getString("swiftCode"), rs.getString("bankName"), rs.getString("address"),
                                        rs.getString("countryISO2"), rs.getBoolean("isHeadquarter")
                                ));
            }
            Country country = new Country(countryISO2code,countryName, branches);


            var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            // Customize the pretty printer with 4-space indentation
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(new DefaultIndenter("    ", "\n")); // 4 spaces for arrays
            prettyPrinter.indentObjectsWith(new DefaultIndenter("    ", "\n")); // 4 spaces for objects
            try {
                return ResponseEntity.ok(mapper.writer(prettyPrinter).writeValueAsString(country));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        catch (SQLException e) {
            return new ResponseEntity<>("message: failed to retrieve branches, sql exception", HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("")
    public ResponseEntity<String> postSwift(@RequestBody Map<String, String> branchOrHq)
    {

        boolean isHeadquarter = Boolean.parseBoolean(branchOrHq.get("isHeadquarter"));
        if (isHeadquarter)
        {
            try {
                if(doubleInsertionCheck("headquarters", branchOrHq.get("swiftCode")))
                {
                    return new ResponseEntity<>("message: post request failed, headquarters with this swiftCode already exist", HttpStatus.BAD_REQUEST);
                }

                String sql = "INSERT INTO headquarters  VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt2 = localdb.getConnection().prepareStatement(sql);
                stmt2.setString(1, branchOrHq.get("address"));
                stmt2.setString(2, branchOrHq.get("bankName"));
                stmt2.setString(3, branchOrHq.get("countryISO2"));
                stmt2.setString(4, branchOrHq.get("countryName"));
                stmt2.setBoolean(5, Boolean.parseBoolean(branchOrHq.get("isHeadquarter")));
                stmt2.setString(6, branchOrHq.get("swiftCode"));
                stmt2.executeUpdate();
            }

            catch (SQLException e) {
                    throw new RuntimeException(e);
                }
        }
        else
        {
            if(doubleInsertionCheck("branches", branchOrHq.get("swiftCode")))
            {
                return new ResponseEntity<>("message: post request failed, branch  with this swiftCode already exist", HttpStatus.BAD_REQUEST);
            }
            try {
            String sqlQuery = "SELECT swiftCode from headquarters WHERE swiftCode like ?";
            String prefix = branchOrHq.get("swiftCode").substring(0,7)+"%";
            PreparedStatement stmt = null;
            stmt = localdb.getConnection().prepareStatement(sqlQuery);
            stmt.setString(1, prefix);
            ResultSet rs = stmt.executeQuery();
            String hqSwift = null;
            if(rs.next())
            {
                hqSwift = rs.getString("swiftCode");
            }
            if(hqSwift == null)
            {
                return new ResponseEntity<>("message: post request failed, failed to retrieve headquarters for this branch", HttpStatus.BAD_REQUEST);
            }
            String sql = "INSERT INTO branches VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt2 = localdb.getConnection().prepareStatement(sql);
            stmt2.setString(1, branchOrHq.get("address"));
            stmt2.setString(2, branchOrHq.get("bankName"));
            stmt2.setString(3, branchOrHq.get("countryISO2"));
            stmt2.setBoolean(4,Boolean.parseBoolean(branchOrHq.get("isHeadquarter")));
            stmt2.setString(5,branchOrHq.get("swiftCode"));
            stmt2.setString(6,hqSwift);
            stmt2.executeUpdate();
            }
            catch (SQLException e) {
            throw new RuntimeException(e);
            }
        }
        return new ResponseEntity<>("message: post request success", HttpStatus.OK);
    }
    private boolean doubleInsertionCheck(String branchesOrHeadquarters, String swiftCode) {
        String sql = "SELECT * FROM " + branchesOrHeadquarters + " WHERE swiftCode = ?";
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
            stmt.setString(1, swiftCode);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<String> deleteSwift(@PathVariable String swiftCode)
    {
        if (swiftCode.matches(".*XXX"))
        {
            String sql = "DELETE FROM headquarters WHERE swiftCode = ?";
            try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
                stmt.setString(1, swiftCode);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    return new ResponseEntity<>("message: delete request success", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("message: no records found to delete", HttpStatus.NOT_FOUND);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new ResponseEntity<>("message: delete request failed", HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            String sql = "DELETE FROM branches WHERE swiftCode = ?";
            try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
                stmt.setString(1, swiftCode);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    return new ResponseEntity<>("message: delete request success", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("message: no records found to delete", HttpStatus.NOT_FOUND);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new ResponseEntity<>("message: delete request failed", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private boolean isHeadquarter(String swiftCode) {
        return swiftCode.matches(".*XXX");
    }

    private Hq getHeadquarter(String swiftCode, List<Branch> branches) {
        String sql = "SELECT address, bankName, countryISO2, countryName, isHeadquarter, swiftCode " +
                "FROM headquarters WHERE swiftCode = ? ";
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
            stmt.setString(1, swiftCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Hq(rs.getString("swiftCode"), rs.getString("bankName"), rs.getString("address"),
                        rs.getString("countryISO2"), rs.getString("countryName"), rs.getBoolean("isHeadquarter"), branches);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private Branch getBranch(String swiftCode){
        String sql = "SELECT address, bankName, countryISO2, isHeadquarter, swiftCode, headquartersSwift " +
                "FROM branches WHERE swiftCode = ? ";
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
            stmt.setString(1, swiftCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Branch(rs.getString("swiftCode"), rs.getString("bankName"), rs.getString("address"),
                        rs.getString("countryISO2"), rs.getBoolean("isHeadquarter"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected List<Branch> getBranches(String hqSwiftCode)
    {
        String sql = "SELECT address, bankName, countryISO2, isHeadquarter, swiftCode, headquartersSwift " +
                "FROM branches WHERE headquartersSwift = ? ";
        List<Branch> branches = new ArrayList<>();
        try (PreparedStatement stmt = localdb.getConnection().prepareStatement(sql)) {
            stmt.setString(1, hqSwiftCode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                branches.add(
                        new Branch
                        (rs.getString("swiftCode"), rs.getString("bankName"), rs.getString("address"),
                        rs.getString("countryISO2"), rs.getBoolean("isHeadquarter")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }
}


