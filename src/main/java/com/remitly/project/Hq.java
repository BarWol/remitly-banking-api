package com.remitly.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Hq {
    @JsonProperty("swiftCode")
    private String swiftCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("countryISO2")
    private String countryISO2;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;

    @JsonProperty("branches")
    private List<Branch> branches;

    public Hq(String swiftCode, String bankName, String address, String countryISO2, String countryName, boolean isHeadquarter, List<Branch> branches) {
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.countryISO2 = countryISO2;
        this.countryName = countryName;
        this.isHeadquarter = isHeadquarter;
        this.branches = branches;
    }

    // Getters and Setters
}