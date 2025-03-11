package com.remitly.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

    public static void main(String[] args) {


        LocalDatabase db = new LocalDatabase();

        db.process_csv("/app/resources/Interns_2025_SWIFT_CODES.csv");


        SpringApplication.run(ProjectApplication.class, args);
    }

}
