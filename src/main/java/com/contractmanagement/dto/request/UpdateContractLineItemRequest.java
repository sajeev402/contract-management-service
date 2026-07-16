package com.contractmanagement.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record UpdateContractLineItemRequest(
        String description,
        @Min(1) Integer quantity,
        @DecimalMin("0.00") BigDecimal unitPrice,
        @DecimalMin("0.00") @DecimalMax("40.00") BigDecimal discountPercentage
) {
}
