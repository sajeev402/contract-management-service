package com.contractmanagement.mapper;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.dto.response.ContractLineItemResponse;
import com.contractmanagement.entity.ContractLineItem;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ContractLineItemMapper {

    public ContractLineItem toEntity(CreateContractLineItemRequest request) {
        if (request == null) {
            return null;
        }

        ContractLineItem item = new ContractLineItem();
        item.setProductCode(request.productCode());
        item.setDescription(request.description());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        if (request.discountPercentage() != null) {
            item.setDiscountPercentage(request.discountPercentage());
        } else {
            item.setDiscountPercentage(BigDecimal.ZERO);
        }
        return item;
    }

    public ContractLineItemResponse toResponse(ContractLineItem item) {
        if (item == null) {
            return null;
        }

        return new ContractLineItemResponse(
                item.getId(),
                item.getContract() != null ? item.getContract().getId() : null,
                item.getProductCode(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountPercentage(),
                item.getLineTotal()
        );
    }
}
