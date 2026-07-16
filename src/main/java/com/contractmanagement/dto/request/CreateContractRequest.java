package com.contractmanagement.dto.request;

import com.contractmanagement.enums.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateContractRequest(
        @NotBlank String customerReference,
        @NotBlank String title,
        @NotNull ContractType contractType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String currency,
        String paymentTerms
) {
}
