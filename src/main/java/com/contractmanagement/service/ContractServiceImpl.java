package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.exception.BusinessException;
import com.contractmanagement.exception.ResourceNotFoundException;
import com.contractmanagement.mapper.ContractMapper;
import com.contractmanagement.repository.ContractRepository;
import com.contractmanagement.util.ContractNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;

    public ContractServiceImpl(ContractRepository contractRepository, ContractMapper contractMapper) {
        this.contractRepository = contractRepository;
        this.contractMapper = contractMapper;
    }

    @Override
    @Transactional
    public ContractResponse createContract(CreateContractRequest request) {
        validateContractDates(request.startDate(), request.endDate());

        Contract contract = contractMapper.toEntity(request);
        // Temporary solution for unique sequence until proper sequence is built
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

        if (contract.getStatus() != ContractStatus.DRAFT) {
            // Note: the requirements don't explicitly forbid updating title/dates in non-draft, 
            // but usually modifications are restricted. For now, allow basic field updates unless it affects lifecycle.
            // BR-02 applies on any update that changes dates.
        }

        LocalDate newStartDate = request.startDate() != null ? request.startDate() : contract.getStartDate();
        LocalDate newEndDate = request.endDate() != null ? request.endDate() : contract.getEndDate();
        
        validateContractDates(newStartDate, newEndDate);

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
