package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.dto.request.UpdateContractLineItemRequest;
import com.contractmanagement.dto.response.ContractLineItemResponse;
import java.util.List;

public interface ContractLineItemService {
    ContractLineItemResponse createLineItem(Long contractId, CreateContractLineItemRequest request);
    ContractLineItemResponse updateLineItem(Long contractId, Long lineItemId, UpdateContractLineItemRequest request);
    void deleteLineItem(Long contractId, Long lineItemId);
    List<ContractLineItemResponse> getLineItems(Long contractId);
}
