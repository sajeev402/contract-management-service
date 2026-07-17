package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;

public interface ContractService {
    ContractResponse createContract(CreateContractRequest request);
    ContractResponse updateContract(Long id, UpdateContractRequest request);
    ContractResponse getContract(Long id);
    void deleteContract(Long id);
    List<ContractResponse> getAllContracts();
    Page<ContractResponse> searchContracts(ContractStatus status, String customerReference, ContractType contractType, BigDecimal minimumNetValue, BigDecimal maximumNetValue, LocalDate startDateFrom, LocalDate startDateTo, LocalDate endDateFrom, LocalDate endDateTo, Pageable pageable);
    ContractResponse submitContract(Long id);
    ContractResponse approveContract(Long id, com.contractmanagement.dto.request.ApprovalRequest request);
    ContractResponse activateContract(Long id);
    ContractResponse terminateContract(Long id, com.contractmanagement.dto.request.TerminateRequest request);
    ContractResponse renewContract(Long id);
    List<ContractResponse> getExpiringContracts(int days);
}
