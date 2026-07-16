package com.contractmanagement.mapper;

import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import com.contractmanagement.entity.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public Contract toEntity(CreateContractRequest request) {
        if (request == null) {
            return null;
        }

        Contract contract = new Contract();
        contract.setCustomerReference(request.customerReference());
        contract.setTitle(request.title());
        contract.setContractType(request.contractType());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setCurrency(request.currency());
        contract.setPaymentTerms(request.paymentTerms());
        return contract;
    }

    public ContractResponse toResponse(Contract contract) {
        if (contract == null) {
            return null;
        }

        return new ContractResponse(
                contract.getId(),
                contract.getContractNumber(),
                contract.getCustomerReference(),
                contract.getTitle(),
                contract.getContractType(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getCurrency(),
                contract.getPaymentTerms(),
                contract.getStatus(),
                contract.getGrossValue(),
                contract.getNetValue(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }
}
