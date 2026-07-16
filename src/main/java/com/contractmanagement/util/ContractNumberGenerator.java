package com.contractmanagement.util;

import java.time.Year;

public final class ContractNumberGenerator {

    private ContractNumberGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a contract number in the format CON-YYYY-NNNNN.
     *
     * @param sequenceNumber The unique sequence number for the year.
     * @return The formatted contract number string.
     */
    public static String generate(int sequenceNumber) {
        int currentYear = Year.now().getValue();
        return generate(currentYear, sequenceNumber);
    }

    /**
     * Generates a contract number in the format CON-YYYY-NNNNN for a specific year.
     *
     * @param year           The year of the contract.
     * @param sequenceNumber The unique sequence number for the year.
     * @return The formatted contract number string.
     */
    public static String generate(int year, int sequenceNumber) {
        if (sequenceNumber < 0 || sequenceNumber > 99999) {
            throw new IllegalArgumentException("Sequence number must be between 0 and 99999");
        }
        return String.format("CON-%04d-%05d", year, sequenceNumber);
    }
}
