package com.remitly.project;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;


class ProjectApplicationTests {

    private static final RestTemplate restTemplate = new RestTemplate();


    @BeforeAll
    static void setUp() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // Treat all status codes as non-errors
            }
        });
    }


    @Test
    void deleteHq() throws SQLException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = "http://localhost:8080/v1/swift-codes/BCHICLRMXXX"; //removing bank the chile
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//	Connection connection =db.getConnection();
//	String sql = "SELECT hq.SwiftCode FROM headquarters hq " +
//			     "JOIN branches b ON hq.SwiftCode = b.headquartersSwift " +
//			     "WHERE b.SwiftCode IS NOT NULL LIMIT 1";
//	PreparedStatement preparedStatement = connection.prepareStatement(sql);
//	ResultSet resultSet = preparedStatement.executeQuery();
//
//	if(resultSet.next())
//		{
//			String swiftCode = resultSet.getString(1);
//			String url = "http://localhost:8080/v1/swift-codes/" + swiftCode;
//			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
//			Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//			Assertions.assertThat(response.getBody()).isNotNull().contains("message: delete request success");
//			System.out.println("Response: " + response.getStatusCode() + " - " + response.getBody());
//			String sql2 = "SELECT SUM(count) FROM (" +
//					"SELECT COUNT(*) AS count FROM headquarters WHERE SwiftCode = ? " +
//					"UNION ALL " +
//					"SELECT COUNT(*) FROM branches WHERE headquartersSwift = ?" +
//					") AS counts";
//			PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
//			preparedStatement2.setString(1, swiftCode);
//			preparedStatement2.setString(2, swiftCode);
//			ResultSet resultSet2 = preparedStatement2.executeQuery();
//			resultSet2.next();
//			int count2 = resultSet2.getInt(1);
//			Assertions.assertThat(count2).isEqualTo(0);
//
//		}
    }


    @Test
    void createSwift() {
        String json = """
                    {
                        "address": "123 Main St",
                        "bankName": "Example Bank",
                        "countryISO2": "US",
                        "countryName": "United States",
                        "isHeadquarter": true,
                        "swiftCode": "EXAMPUSXXX"
                    }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        String url = "http://localhost:8080/v1/swift-codes";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull().contains("message: post request success");
        System.out.println("Response: " + response.getStatusCode() + " - " + response.getBody());


    }

    @Test
    void testCreateSameSwiftSecondTime() {


        String json = """
                    {
                        "address": "123 Main St",
                        "bankName": "Example Bank",
                        "countryISO2": "US",
                        "countryName": "United States",
                        "isHeadquarter": true,
                        "swiftCode": "EXATPUQXXX"
                    }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        String url = "http://localhost:8080/v1/swift-codes";

        // First POST to create the SWIFT code
        ResponseEntity<String> firstResponse = restTemplate.postForEntity(url, request, String.class);
        System.out.println("First response: " + firstResponse.getStatusCode() + " - " + firstResponse.getBody());

        // Second POST expecting 400
        ResponseEntity<String> secondResponse = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        System.out.println("Second response: " + secondResponse.getStatusCode() + " - " + secondResponse.getBody());

        Assertions.assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(secondResponse.getBody())
                .isNotNull()
                .isEqualTo("message: post request failed, headquarters with this swiftCode already exist");

    }

    @Test
    void postBranchWithoutHq() {

        String branchJson = """
                	{
                		"address": "123 Main St",
                		"bankName": "Example Bank",
                		"countryISO2": "US",
                		"countryName": "United States",
                		"isHeadquarter": false,
                		"swiftCode": "BRANCHHCDC"
                	}
                """;
        HttpHeaders branchHeaders = new HttpHeaders();
        branchHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> branchRequest = new HttpEntity<>(branchJson, branchHeaders);

        String branchUrl = "http://localhost:8080/v1/swift-codes";
        ResponseEntity<String> branchResponse = restTemplate.postForEntity(branchUrl, branchRequest, String.class);

        Assertions.assertThat(branchResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(branchResponse.getBody()).isNotNull().contains("message: post request failed, failed to retrieve headquarters for this branch");
    }

    @Test
    void getCountrySwifts() {
        String url = "http://localhost:8080/v1/swift-codes/country/PL";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
    }

    @Test
    void getHqMbankSwifts() {
        String url = "http://localhost:8080/v1/swift-codes/BREXPLPWXXX";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
    }

    @Test
    void getBranch() {
        String url = "http://localhost:8080/v1/swift-codes/BSCHCLR10R3";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
    }

    @Test
    void getNonExistentBranch() {
        String url = "http://localhost:8080/v1/swift-codes/123HCLR10R4";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
    }

    @Test
    void getNonExistentHq() {
        String url = "http://localhost:8080/v1/swift-codes/123HCLR1XXX";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
    }

    @Test
    void getNonExistentCountry() {
        String url = "http://localhost:8080/v1/swift-codes/country/KK";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
    }

    @Test
    void accessBadUrl() {
        String url = "http://localhost:8080/v1/swXXXdes/123HCLR1XXX";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
    }

}