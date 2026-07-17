package com.contractmanagement.service;

import com.contractmanagement.dto.request.ApprovalRequest;
import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.TerminateRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.entity.ContractApproval;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.exception.BusinessException;
import com.contractmanagement.exception.ResourceNotFoundException;
import com.contractmanagement.mapper.ContractMapper;
import com.contractmanagement.repository.ContractApprovalRepository;
import com.contractmanagement.repository.ContractRepository;
import com.contractmanagement.util.ContractNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractApprovalRepository contractApprovalRepository;
    private final ContractMapper contractMapper;

    public ContractServiceImpl(ContractRepository contractRepository,
                               ContractApprovalRepository contractApprovalRepository,
                               ContractMapper contractMapper) {
        this.contractRepository = contractRepository;
        this.contractApprovalRepository = contractApprovalRepository;
        this.contractMapper = contractMapper;
    }

    @Override
    @Transactional
    public ContractResponse createContract(CreateContractRequest request) {
        validateContractDates(request.startDate(), request.endDate());

        Contract contract = contractMapper.toEntity(request);
        int randomSeq = (int) (Math.random() * 90000) + 10000;
        contract.setContractNumber(ContractNumberGenerator.generate(randomSeq));
        contract.setStatus(ContractStatus.DRAFT);
        contract.setGrossValue(BigDecimal.ZERO);
        contract.setNetValue(BigDecimal.ZERO);
        contract.setCreatedAt(OffsetDateTime.now());
        contract.setUpdatedAt(OffsetDateTime.now());
        contract.setDeleted(false);

        Contract savedContract = contractRepository.save(contract);
        return contractMapper.toResponse(savedContract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(Long id, UpdateContractRequest request) {
        Contract contract = getContractEntity(id);

        if (contract.getStatus() == ContractStatus.TERMINATED || contract.getStatus() == ContractStatus.EXPIRED) {
            throw new BusinessException("Cannot update a contract in a terminal state.");
        }

        LocalDate newStartDate = request.startDate() != null ? request.startDate() : contract.getStartDate();
        LocalDate newEndDate = request.endDate() != null ? request.endDate() : contract.getEndDate();
        
        validateContractDates(newStartDate, newEndDate);
        
        if (contract.getStatus() == ContractStatus.PENDING_APPROVAL) {
            if (request.startDate() != null || request.endDate() != null) {
                contract.setStatus(ContractStatus.DRAFT);
                contract.setApprovedBy(null);
                contract.setApprovedAt(null);
            }
        }

        if (request.title() != null) {
            contract.setTitle(request.title());
        }
        if (request.startDate() != null) {
            contract.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            contract.setEndDate(request.endDate());
        }
        if (request.paymentTerms() != null) {
            contract.setPaymentTerms(request.paymentTerms());
        }
        
        contract.setUpdatedAt(OffsetDateTime.now());

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContract(Long id) {
        return contractMapper.toResponse(getContractEntity(id));
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        Contract contract = getContractEntity(id);
        if (contract.getStatus() == ContractStatus.TERMINATED || contract.getStatus() == ContractStatus.EXPIRED) {
            throw new BusinessException("Cannot delete a contract in a terminal state.");
        }
        contract.setDeleted(true);
        contract.setUpdatedAt(OffsetDateTime.now());
        contractRepository.save(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractResponse submitContract(Long id) {
        Contract contract = getContractEntity(id);
        
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException(String.format("Contract cannot move from %s to PENDING_APPROVAL/ACTIVE. Allowed from DRAFT.", contract.getStatus()));
        }
        
        boolean requiresApproval = requiresApproval(contract);
        
        if (requiresApproval) {
            contract.setStatus(ContractStatus.PENDING_APPROVAL);
        } else {
            validateActivation(contract);
            contract.setStatus(ContractStatus.ACTIVE);
        }
        
        contract.setUpdatedAt(OffsetDateTime.now());
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse approveContract(Long id, ApprovalRequest request) {
        Contract contract = getContractEntity(id);
        
        if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
            throw new BusinessException(String.format("Contract cannot move from %s to ACTIVE. Allowed from PENDING_APPROVAL.", contract.getStatus()));
        }
        
        ContractApproval approval = new ContractApproval();
        approval.setContract(contract);
        approval.setApproverName(request.approverName());
        approval.setRemarks(request.remarks());
        approval.setApprovedAt(OffsetDateTime.now());
        contractApprovalRepository.save(approval);
        
        contract.setApprovedBy(request.approverName());
        contract.setApprovedAt(OffsetDateTime.now());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setUpdatedAt(OffsetDateTime.now());
        
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse activateContract(Long id) {
        Contract contract = getContractEntity(id);
        
        if (contract.getStatus() != ContractStatus.DRAFT && contract.getStatus() != ContractStatus.PENDING_APPROVAL) {
            throw new BusinessException(
                String.format("Contract cannot move from %s to ACTIVE. Allowed from DRAFT or PENDING_APPROVAL.", contract.getStatus())
            );
        }
        
        if (contract.getStatus() == ContractStatus.DRAFT && requiresApproval(contract)) {
            throw new BusinessException("Contract requires approval because effective discount is greater than 15%.");
        }
        
        validateActivation(contract);
        
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setUpdatedAt(OffsetDateTime.now());
        
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public ContractResponse terminateContract(Long id, TerminateRequest request) {
        Contract contract = getContractEntity(id);
        
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException(String.format("Contract cannot move from %s to TERMINATED. Allowed from ACTIVE.", contract.getStatus()));
        }
        
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminationReason(request.terminationReason());
        contract.setTerminationDate(OffsetDateTime.now());
        contract.setUpdatedAt(OffsetDateTime.now());
        
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    private boolean requiresApproval(Contract contract) {
        if (contract.getGrossValue() == null || contract.getGrossValue().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        
        BigDecimal discountAmount = contract.getGrossValue().subtract(contract.getNetValue());
        BigDecimal effectiveDiscount = discountAmount.divide(contract.getGrossValue(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        
        return effectiveDiscount.compareTo(new BigDecimal("15.00")) > 0;
    }
    
    private void validateActivation(Contract contract) {
        if (contract.getLineItems() == null || contract.getLineItems().isEmpty()) {
            throw new BusinessException("Activation requires at least one line item.");
        }
        
        if (contract.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Contract end date has already passed. Cannot activate.");
        }
    }

    private Contract getContractEntity(Long id) {
        return contractRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
    }

    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new BusinessException("Contract end date must be after start date.");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween < 30) {
            throw new BusinessException("Contract minimum duration is 30 days.");
        }

        long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate);
        if (monthsBetween > 60 || (monthsBetween == 60 && startDate.plusMonths(60).isBefore(endDate))) {
            throw new BusinessException("Contract maximum duration is 60 months.");
        }
    }
}
