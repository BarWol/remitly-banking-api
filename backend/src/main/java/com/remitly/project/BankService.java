package com.remitly.project;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BankService {

    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public Map<String, Object> getBankDetails(String swiftCode) {
        List<Object[]> results = bankRepository.findHeadquarterWithBranches(swiftCode);

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> headquarter = Map.of(
                "swiftCode", results.get(0)[0],
                "bankName", results.get(0)[1],
                "address", results.get(0)[2],
                "countryISO2", results.get(0)[3],
                "countryName", results.get(0)[4],
                "isHeadquarter", results.get(0)[5],
                "branches", results.stream()
                        .filter(row -> row[6] != null) // Exclude cases where there are no branches
                        .map(row -> Map.of(
                                "swiftCode", row[6],
                                "bankName", row[7],
                                "address", row[8],
                                "countryISO2", row[9],
                                "isHeadquarter", row[10]
                        ))
                        .collect(Collectors.toList())
        );

        return headquarter;
    }
}
