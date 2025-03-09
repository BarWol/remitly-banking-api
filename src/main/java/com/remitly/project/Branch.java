package com.remitly.project;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Branch {
    @JsonProperty("swiftCode")
    private String swiftCode;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("countryISO2")
    private String countryISO2;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;

    public Branch(String swiftCode, String bankName, String address, String countryISO2, boolean isHeadquarter) {
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.countryISO2 = countryISO2;
        this.isHeadquarter = isHeadquarter;
    }

    // Getters and Setters
}