package com.contractmanagement.dto.response;

import java.math.BigDecimal;

public record ContractLineItemResponse(
        Long id,
        Long contractId,
        String productCode,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercentage,
        BigDecimal lineTotal
) {
}
