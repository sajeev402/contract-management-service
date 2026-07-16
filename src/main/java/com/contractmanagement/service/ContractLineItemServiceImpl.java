package com.contractmanagement.service;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.dto.request.UpdateContractLineItemRequest;
import com.contractmanagement.dto.response.ContractLineItemResponse;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.entity.ContractLineItem;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.exception.BusinessException;
import com.contractmanagement.exception.ResourceNotFoundException;
import com.contractmanagement.mapper.ContractLineItemMapper;
import com.contractmanagement.repository.ContractLineItemRepository;
import com.contractmanagement.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractLineItemServiceImpl implements ContractLineItemService {

    private final ContractLineItemRepository lineItemRepository;
    private final ContractRepository contractRepository;
    private final ContractLineItemMapper lineItemMapper;

    public ContractLineItemServiceImpl(ContractLineItemRepository lineItemRepository,
                                       ContractRepository contractRepository,
                                       ContractLineItemMapper lineItemMapper) {
        this.lineItemRepository = lineItemRepository;
        this.contractRepository = contractRepository;
        this.lineItemMapper = lineItemMapper;
    }

    @Override
    @Transactional
    public ContractLineItemResponse createLineItem(Long contractId, CreateContractLineItemRequest request) {
        Contract contract = getContract(contractId);
        validateContractDraftStatus(contract);
        validateMaxLineItems(contract);

        ContractLineItem item = lineItemMapper.toEntity(request);
        item.setContract(contract);
        item.setLineTotal(calculateLineTotal(item.getQuantity(), item.getUnitPrice(), item.getDiscountPercentage()));

        ContractLineItem savedItem = lineItemRepository.save(item);
        contract.getLineItems().add(savedItem);
        recalculateContractValues(contract);
        contractRepository.save(contract);

        return lineItemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public ContractLineItemResponse updateLineItem(Long contractId, Long lineItemId, UpdateContractLineItemRequest request) {
        Contract contract = getContract(contractId);
        validateContractDraftStatus(contract);

        ContractLineItem item = getLineItem(lineItemId);
        if (!item.getContract().getId().equals(contractId)) {
            throw new BusinessException("Line item does not belong to this contract.");
        }

        if (request.description() != null) {
            item.setDescription(request.description());
        }
        if (request.quantity() != null) {
            item.setQuantity(request.quantity());
        }
        if (request.unitPrice() != null) {
            item.setUnitPrice(request.unitPrice());
        }
        if (request.discountPercentage() != null) {
            item.setDiscountPercentage(request.discountPercentage());
        }

        item.setLineTotal(calculateLineTotal(item.getQuantity(), item.getUnitPrice(), item.getDiscountPercentage()));
        ContractLineItem savedItem = lineItemRepository.save(item);

        recalculateContractValues(contract);
        contractRepository.save(contract);

        return lineItemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteLineItem(Long contractId, Long lineItemId) {
        Contract contract = getContract(contractId);
        validateContractDraftStatus(contract);

        ContractLineItem item = getLineItem(lineItemId);
        if (!item.getContract().getId().equals(contractId)) {
            throw new BusinessException("Line item does not belong to this contract.");
        }

        contract.getLineItems().remove(item);
        lineItemRepository.delete(item);

        recalculateContractValues(contract);
        contractRepository.save(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractLineItemResponse> getLineItems(Long contractId) {
        Contract contract = getContract(contractId);
        return contract.getLineItems().stream()
                .map(lineItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Contract getContract(Long contractId) {
        return contractRepository.findById(contractId)
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));
    }

    private ContractLineItem getLineItem(Long lineItemId) {
        return lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Line item not found with id: " + lineItemId));
    }

    private void validateContractDraftStatus(Contract contract) {
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException("Line items can only be modified while the contract is in DRAFT state.");
        }
    }

    private void validateMaxLineItems(Contract contract) {
        if (contract.getLineItems().size() >= 100) {
            throw new BusinessException("A contract can carry at most 100 line items.");
        }
    }

    private BigDecimal calculateLineTotal(Integer quantity, BigDecimal unitPrice, BigDecimal discountPercentage) {
        BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return unitPrice.multiply(new BigDecimal(quantity))
                .multiply(discountFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void recalculateContractValues(Contract contract) {
        BigDecimal grossValue = BigDecimal.ZERO;
        BigDecimal netValue = BigDecimal.ZERO;

        for (ContractLineItem item : contract.getLineItems()) {
            BigDecimal undiscountedTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            grossValue = grossValue.add(undiscountedTotal);
            netValue = netValue.add(item.getLineTotal());
        }

        contract.setGrossValue(grossValue);
        contract.setNetValue(netValue);
    }
}
