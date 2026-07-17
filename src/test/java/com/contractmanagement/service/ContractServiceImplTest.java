package com.contractmanagement.service;

import com.contractmanagement.dto.request.ApprovalRequest;
import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.TerminateRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.entity.ContractApproval;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;
import com.contractmanagement.exception.BusinessException;
import com.contractmanagement.mapper.ContractMapper;
import com.contractmanagement.repository.ContractApprovalRepository;
import com.contractmanagement.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractApprovalRepository contractApprovalRepository;

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract contract;
    private ContractResponse contractResponse;

    @BeforeEach
    void setUp() {
        contract = Contract.builder()
                .id(1L)
                .contractNumber("CTR-12345")
                .customerReference("CUST-001")
                .title("Test Contract")
                .contractType(ContractType.LEASE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(60))
                .currency("USD")
                .status(ContractStatus.DRAFT)
                .grossValue(BigDecimal.ZERO)
                .netValue(BigDecimal.ZERO)
                .deleted(false)
                .build();

        contractResponse = new ContractResponse(
                1L, "CTR-12345", "CUST-001", "Test Contract", ContractType.LEASE,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(60), "USD", null, ContractStatus.DRAFT,
                BigDecimal.ZERO, BigDecimal.ZERO, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    void createContract_Success() {
        CreateContractRequest request = new CreateContractRequest("CUST-001", "Test Contract", ContractType.LEASE,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(60), "USD", null);

        when(contractMapper.toEntity(any())).thenReturn(contract);
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        ContractResponse response = contractService.createContract(request);

        assertNotNull(response);
        assertEquals("CTR-12345", response.contractNumber());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void createContract_FailsWhenDurationLessThan30Days() {
        CreateContractRequest request = new CreateContractRequest("CUST-001", "Test Contract", ContractType.LEASE,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(20), "USD", null);

        BusinessException ex = assertThrows(BusinessException.class, () -> contractService.createContract(request));
        assertEquals("Contract minimum duration is 30 days.", ex.getMessage());
    }

    @Test
    void updateContract_Success() {
        UpdateContractRequest request = new UpdateContractRequest("Updated Title", null, null, null);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        contractService.updateContract(1L, request);

        verify(contractRepository).save(contract);
        assertEquals("Updated Title", contract.getTitle());
    }

    @Test
    void updateContract_FailsWhenTerminalState() {
        contract.setStatus(ContractStatus.TERMINATED);
        UpdateContractRequest request = new UpdateContractRequest("Updated Title", null, null, null);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        BusinessException ex = assertThrows(BusinessException.class, () -> contractService.updateContract(1L, request));
        assertEquals("Cannot update a contract in a terminal state.", ex.getMessage());
    }

    @Test
    void deleteContract_Success() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        contractService.deleteContract(1L);
        verify(contractRepository).save(contract);
        assertEquals(true, contract.getDeleted());
    }

    @Test
    void submitContract_Success_NoApprovalRequired() {
        contract.getLineItems().add(new com.contractmanagement.entity.ContractLineItem());
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        contractService.submitContract(1L);

        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
    }

    @Test
    void submitContract_RequiresApproval() {
        contract.setGrossValue(new BigDecimal("1000.00"));
        contract.setNetValue(new BigDecimal("800.00")); // 20% discount
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        contractService.submitContract(1L);

        assertEquals(ContractStatus.PENDING_APPROVAL, contract.getStatus());
    }

    @Test
    void approveContract_Success() {
        contract.setStatus(ContractStatus.PENDING_APPROVAL);
        ApprovalRequest request = new ApprovalRequest("Approver", "Looks good");
        
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        contractService.approveContract(1L, request);

        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
        assertEquals("Approver", contract.getApprovedBy());
        verify(contractApprovalRepository).save(any());
    }

    @Test
    void renewContract_Success() {
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setEndDate(LocalDate.now().plusDays(10)); // Eligible (<= 90 days)
        
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.findAll()).thenReturn(Collections.emptyList());
        when(contractRepository.save(any())).thenReturn(contract);
        when(contractMapper.toResponse(any())).thenReturn(contractResponse);

        contractService.renewContract(1L);

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        
        Contract newContract = captor.getValue();
        assertEquals(ContractStatus.DRAFT, newContract.getStatus());
        assertNotNull(newContract.getParentContract());
    }

    @Test
    void submitContract_FailsWhenNotDraft() {
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        BusinessException ex = assertThrows(BusinessException.class, () -> contractService.submitContract(1L));
        assertEquals("Contract cannot move from ACTIVE to PENDING_APPROVAL/ACTIVE. Allowed from DRAFT.", ex.getMessage());
    }
}
