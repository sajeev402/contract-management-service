package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import java.util.List;

public interface ContractService {
    ContractResponse createContract(CreateContractRequest request);
    ContractResponse updateContract(Long id, UpdateContractRequest request);
    ContractResponse getContract(Long id);
    void deleteContract(Long id);
    List<ContractResponse> getAllContracts();
    ContractResponse submitContract(Long id);
    ContractResponse approveContract(Long id, com.contractmanagement.dto.request.ApprovalRequest request);
    ContractResponse activateContract(Long id);
    ContractResponse terminateContract(Long id, com.contractmanagement.dto.request.TerminateRequest request);
}
