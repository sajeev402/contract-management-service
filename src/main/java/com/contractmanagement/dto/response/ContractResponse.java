package com.contractmanagement.dto.response;

import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ContractResponse(
        Long id,
        String contractNumber,
        String customerReference,
        String title,
        ContractType contractType,
        LocalDate startDate,
        LocalDate endDate,
        String currency,
        String paymentTerms,
        ContractStatus status,
        BigDecimal grossValue,
        BigDecimal netValue,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
