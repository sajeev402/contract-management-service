package com.contractmanagement.controller;

import com.contractmanagement.dto.request.ApprovalRequest;
import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.dto.request.TerminateRequest;
import com.contractmanagement.dto.request.UpdateContractRequest;
import com.contractmanagement.dto.response.ContractResponse;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;
import com.contractmanagement.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contracts", description = "Contract Management APIs")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    @Operation(summary = "Create a new contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contract created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody CreateContractRequest request) {
        return new ResponseEntity<>(contractService.createContract(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contract by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<ContractResponse> getContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContract(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> updateContract(@PathVariable Long id, @Valid @RequestBody UpdateContractRequest request) {
        return ResponseEntity.ok(contractService.updateContract(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contract deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all contracts with optional filtering and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contracts retrieved successfully")
    })
    public ResponseEntity<Page<ContractResponse>> getAllContracts(
            @Parameter(description = "Contract status") @RequestParam(required = false) ContractStatus status,
            @Parameter(description = "Customer reference") @RequestParam(required = false) String customerReference,
            @Parameter(description = "Contract type") @RequestParam(required = false) ContractType contractType,
            @Parameter(description = "Minimum net value") @RequestParam(required = false) BigDecimal minimumNetValue,
            @Parameter(description = "Maximum net value") @RequestParam(required = false) BigDecimal maximumNetValue,
            @Parameter(description = "Start date from") @RequestParam(required = false) LocalDate startDateFrom,
            @Parameter(description = "Start date to") @RequestParam(required = false) LocalDate startDateTo,
            @Parameter(description = "End date from") @RequestParam(required = false) LocalDate endDateFrom,
            @Parameter(description = "End date to") @RequestParam(required = false) LocalDate endDateTo,
            @ParameterObject Pageable pageable) {
        
        return ResponseEntity.ok(contractService.searchContracts(
                status, customerReference, contractType, minimumNetValue, maximumNetValue,
                startDateFrom, startDateTo, endDateFrom, endDateTo, pageable));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring contracts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expiring contracts retrieved successfully")
    })
    public ResponseEntity<List<ContractResponse>> getExpiringContracts(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(contractService.getExpiringContracts(days));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew an eligible contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract renewed successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> renewContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.renewContract(id));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a contract for approval or activation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract submitted successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> submitContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.submitContract(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> approveContract(@PathVariable Long id, @Valid @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(contractService.approveContract(id, request));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract activated successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> activateContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.activateContract(id));
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate an active contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contract terminated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractResponse> terminateContract(@PathVariable Long id, @Valid @RequestBody TerminateRequest request) {
        return ResponseEntity.ok(contractService.terminateContract(id, request));
    }
}
