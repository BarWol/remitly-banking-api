package com.remitly.project;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

@Component
public class LocalDatabase implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Connection connection;
    private int port;
    private String username;
    private String database;


    public LocalDatabase(int port, String host) {
        this.port = port;
        this.database = "example";
        this.username = "postgres";
        String password = getPassword();
        this.connect(password, host);

    }

    public LocalDatabase() {
        this.port = 5_432;
        this.database = "example";
        this.username = "postgres";
        String password = getPassword();
        this.connect(password, "db");

    }
    protected String getPassword() {
        try {
            return new String(Files.readAllBytes(Paths.get("/run/secrets/db-password"))).trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read database password from environment variable", e);
        }
    }
    public Connection getConnection() {
        return connection;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void connect(String password, String host) {
        Properties props = new Properties();
        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        props.setProperty("user", username);
        props.setProperty("password", password);
        Connection conn = null;

        int retries = 5;
        int backoff = 1;
        SQLException lastException = null;
        while (conn == null && retries-- > 0) {
            try {
                conn = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                lastException = e;
                logger.warn("Failed to connect to PostgreSQL! Retrying in {}s. Error: {}", backoff, e.getMessage());
            }
            try {
                Thread.sleep(backoff * 1000);
            } catch (InterruptedException ex) {
                break;
            }
            backoff *= 2;
        }
        connection = conn;
        if (conn != null) {
            logger.info("Successfully connected to {}", url);
        } else {
            logger.error("Failed to connect to the PostgreSQL database! No queries will succeed.", lastException);
        }

    }

    @Override
    public void close() throws Exception {
        connection.close();
    }


    public void create_tables() {
        String sql = "CREATE TABLE IF NOT EXISTS headquarters (\n" +
                "    address VARCHAR(255),\n" +
                "    bankName VARCHAR(100) NOT NULL,\n" +
                "    countryISO2 VARCHAR(2) NOT NULL,\n" +
                "    countryName VARCHAR(100),\n" +
                "    isHeadquarter BOOLEAN DEFAULT TRUE,\n" +
                "    swiftCode VARCHAR(11) PRIMARY KEY\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS branches (\n" +
                "    address VARCHAR(255),\n" +
                "    bankName VARCHAR(100) NOT NULL,\n" +
                "    countryISO2 VARCHAR(2) NOT NULL,\n" +
                "    isHeadquarter BOOLEAN DEFAULT FALSE,\n" +
                "    swiftCode VARCHAR(11) UNIQUE NOT NULL,\n" +
                "    headquartersSwift VARCHAR(11) NOT NULL,\n" +
                "    FOREIGN KEY (headquartersSwift) REFERENCES headquarters(swiftCode) ON DELETE CASCADE\n" +
                ");";
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void process_csv(String csv_file_path) {
        this.create_tables();
        try {
            FileReader reader = new FileReader(csv_file_path);
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : parser) {
                String swiftCode = record.get("SWIFT CODE");
                String checkSQL = "SELECT COUNT(*) FROM headquarters WHERE swiftCode = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkSQL);
                checkStmt.setString(1, swiftCode);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {  // Only insert if it doesn't exist
                    if (swiftCode.matches(".*XXX")) {
                        String insertSQL = "INSERT INTO headquarters (address, bankName, countryISO2, countryName, isHeadquarter, swiftCode) VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                        preparedStatement.setString(1, record.get("ADDRESS"));
                        preparedStatement.setString(2, record.get("NAME"));
                        preparedStatement.setString(3, record.get("COUNTRY ISO2 CODE"));
                        preparedStatement.setString(4, record.get("COUNTRY NAME"));
                        preparedStatement.setBoolean(5, true);
                        preparedStatement.setString(6, swiftCode);
                        preparedStatement.executeUpdate();
                    }
                }
                logger.atTrace().log("Inserted headquarters with swift code {}", swiftCode);

            }

            // Reset reader and do second pass for branches
            FileReader reader2 = new FileReader(csv_file_path);
            CSVParser parser2 = new CSVParser(reader2, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : parser2) {
                String swiftCode = record.get("SWIFT CODE");
                if (!swiftCode.matches(".*XXX")) {
                    String prefix = swiftCode.substring(0, 7);
                    String insertSQL = "INSERT INTO branches (address, bankName, countryISO2,  isHeadquarter, swiftCode,headquartersSwift) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                    preparedStatement.setString(1, record.get("ADDRESS"));
                    preparedStatement.setString(2, record.get("NAME"));
                    preparedStatement.setString(3, record.get("COUNTRY ISO2 CODE"));
                    preparedStatement.setBoolean(4, false);
                    preparedStatement.setString(5, swiftCode);
                    preparedStatement.setString(6, getHqSwiftCode(prefix));
                    preparedStatement.executeUpdate();
                }
            }
            System.out.println("Data successfully inserted into the database.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private String getHqSwiftCode(String prefix) {
        String sql = "SELECT swiftCode FROM headquarters WHERE swiftCode like ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("swiftCode");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
