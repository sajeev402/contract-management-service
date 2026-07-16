package com.contractmanagement.dto.request;

import java.time.LocalDate;

public record UpdateContractRequest(
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String paymentTerms
) {
}
