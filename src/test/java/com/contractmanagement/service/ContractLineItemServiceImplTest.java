package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.dto.request.UpdateContractLineItemRequest;
import com.contractmanagement.dto.response.ContractLineItemResponse;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.entity.ContractLineItem;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.exception.BusinessException;
import com.contractmanagement.mapper.ContractLineItemMapper;
import com.contractmanagement.repository.ContractLineItemRepository;
import com.contractmanagement.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractLineItemServiceImplTest {

    @Mock
    private ContractLineItemRepository lineItemRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractLineItemMapper lineItemMapper;

    @InjectMocks
    private ContractLineItemServiceImpl lineItemService;

    private Contract contract;
    private ContractLineItem lineItem;
    private ContractLineItemResponse response;

    @BeforeEach
    void setUp() {
        contract = new Contract();
        contract.setId(1L);
        contract.setStatus(ContractStatus.DRAFT);
        contract.setLineItems(new ArrayList<>());
        contract.setGrossValue(BigDecimal.ZERO);
        contract.setNetValue(BigDecimal.ZERO);

        lineItem = new ContractLineItem();
        lineItem.setId(10L);
        lineItem.setContract(contract);
        lineItem.setQuantity(2);
        lineItem.setUnitPrice(new BigDecimal("100.00"));
        lineItem.setDiscountPercentage(new BigDecimal("10.00"));

        response = new ContractLineItemResponse(10L, 1L, "PROD", "Desc", 2, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("180.00"));
    }

    @Test
    void createLineItem_Success() {
        CreateContractLineItemRequest request = new CreateContractLineItemRequest("PROD", "Desc", 2, new BigDecimal("100.00"), new BigDecimal("10.00"));

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(lineItemMapper.toEntity(any())).thenReturn(lineItem);
        when(lineItemRepository.save(any())).thenReturn(lineItem);
        when(lineItemMapper.toResponse(any())).thenReturn(response);

        ContractLineItemResponse result = lineItemService.createLineItem(1L, request);

        assertEquals(new BigDecimal("180.00"), lineItem.getLineTotal());
        assertEquals(new BigDecimal("200.00"), contract.getGrossValue());
        assertEquals(new BigDecimal("180.00"), contract.getNetValue());
        verify(contractRepository).save(contract);
    }

    @Test
    void createLineItem_FailsWhenNotDraft() {
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        CreateContractLineItemRequest request = new CreateContractLineItemRequest("PROD", "Desc", 2, new BigDecimal("100.00"), new BigDecimal("10.00"));

        assertThrows(BusinessException.class, () -> lineItemService.createLineItem(1L, request));
    }

    @Test
    void updateLineItem_Success() {
        contract.getLineItems().add(lineItem);
        UpdateContractLineItemRequest request = new UpdateContractLineItemRequest("Updated", 3, new BigDecimal("100.00"), BigDecimal.ZERO);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(lineItemRepository.findById(10L)).thenReturn(Optional.of(lineItem));
        when(lineItemRepository.save(any())).thenReturn(lineItem);
        when(lineItemMapper.toResponse(any())).thenReturn(response);

        lineItemService.updateLineItem(1L, 10L, request);

        assertEquals(3, lineItem.getQuantity());
        assertEquals(new BigDecimal("300.00"), lineItem.getLineTotal());
        assertEquals(new BigDecimal("300.00"), contract.getGrossValue());
    }

    @Test
    void deleteLineItem_Success() {
        contract.getLineItems().add(lineItem);
        contract.setGrossValue(new BigDecimal("200.00"));
        contract.setNetValue(new BigDecimal("180.00"));
        
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(lineItemRepository.findById(10L)).thenReturn(Optional.of(lineItem));

        lineItemService.deleteLineItem(1L, 10L);

        assertEquals(BigDecimal.ZERO, contract.getGrossValue());
        assertEquals(BigDecimal.ZERO, contract.getNetValue());
        verify(lineItemRepository).delete(lineItem);
        verify(contractRepository).save(contract);
    }
}
