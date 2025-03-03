package com.remitly.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//@SpringBootTest
//@Testcontainers
class ProjectApplicationTests {
//	@Container
//	private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
//			.withDatabaseName("mydb")
//			.withUsername("postgres")
//			.withPassword("remitly-4343");
//
//	static {
//		System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
//		System.setProperty("spring.datasource.username", postgres.getUsername());
//		System.setProperty("spring.datasource.password", postgres.getPassword());
//	}

	@Test
	void contextLoads() {
		// Test logic

	}
}