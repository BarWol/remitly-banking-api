package com.remitly.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, String> {

    @Query(value = """
        SELECT 
            h.swift_code AS headquarterSwiftCode,
            h.bank_name AS headquarterBankName,
            h.address AS headquarterAddress,
            h.country_iso2 AS headquarterCountryISO2,
            h.country_name AS headquarterCountryName,
            h.is_headquarter AS headquarterIsHeadquarter,
            b.swift_code AS branchSwiftCode,
            b.bank_name AS branchBankName,
            b.address AS branchAddress,
            b.country_iso2 AS branchCountryISO2,
            b.is_headquarter AS branchIsHeadquarter
        FROM banks h
        LEFT JOIN branches br ON h.swift_code = br.headquarter_swift
        LEFT JOIN banks b ON br.branch_swift = b.swift_code
        WHERE h.is_headquarter = TRUE AND h.swift_code = :swiftCode
    """, nativeQuery = true)
    List<Object[]> findHeadquarterWithBranches(@Param("swiftCode") String swiftCode);
}
