package com.remitly.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Country {
    @JsonProperty("countryISO2")
    private String countryISO2;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("swiftCodes")
    private List<Branch> swiftCodes;


    Country(String countryISO2, String countryName, List<Branch> swiftCodes) {
        this.countryISO2 = countryISO2;
        this.countryName = countryName;
        this.swiftCodes = swiftCodes;
    }
}
