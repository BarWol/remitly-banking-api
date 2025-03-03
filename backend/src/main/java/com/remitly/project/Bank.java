package com.remitly.project;

import jakarta.persistence.*;

@Entity
@Table(name = "banks")
public class Bank {

    @Id
    @Column(name = "swift_code", length = 11, nullable = false, unique = true)
    private String swiftCode;

    @Column(name = "country_iso2", length = 2, nullable = false)
    private String countryISO2;

    @Column(name = "code_type", length = 10)
    private String codeType;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "town_name")
    private String townName;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "time_zone")
    private String timeZone;

    public Bank() {}

    public Bank(String swiftCode, String countryISO2, String codeType, String bankName, String address, String townName, String countryName, String timeZone) {
        this.swiftCode = swiftCode;
        this.countryISO2 = countryISO2;
        this.codeType = codeType;
        this.bankName = bankName;
        this.address = address;
        this.townName = townName;
        this.countryName = countryName;
        this.timeZone = timeZone;
    }

    // Getters and Setters
    public String getSwiftCode() { return swiftCode; }
    public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }

    public String getCountryISO2() { return countryISO2; }
    public void setCountryISO2(String countryISO2) { this.countryISO2 = countryISO2; }

    public String getCodeType() { return codeType; }
    public void setCodeType(String codeType) { this.codeType = codeType; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTownName() { return townName; }
    public void setTownName(String townName) { this.townName = townName; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}
