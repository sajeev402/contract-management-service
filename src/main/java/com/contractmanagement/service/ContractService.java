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
}
