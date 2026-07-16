package com.contractmanagement.controller;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.dto.request.UpdateContractLineItemRequest;
import com.contractmanagement.dto.response.ContractLineItemResponse;
import com.contractmanagement.service.ContractLineItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/line-items")
@Tag(name = "Contract Line Items", description = "Contract Line Item Management APIs")
public class ContractLineItemController {

    private final ContractLineItemService lineItemService;

    public ContractLineItemController(ContractLineItemService lineItemService) {
        this.lineItemService = lineItemService;
    }

    @PostMapping
    @Operation(summary = "Create a new line item for a contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Line item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractLineItemResponse> createLineItem(
            @PathVariable Long contractId,
            @Valid @RequestBody CreateContractLineItemRequest request) {
        return new ResponseEntity<>(lineItemService.createLineItem(contractId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all line items for a contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found")
    })
    public ResponseEntity<List<ContractLineItemResponse>> getLineItems(@PathVariable Long contractId) {
        return ResponseEntity.ok(lineItemService.getLineItems(contractId));
    }

    @PutMapping("/{lineItemId}")
    @Operation(summary = "Update a line item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Line item or contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ContractLineItemResponse> updateLineItem(
            @PathVariable Long contractId,
            @PathVariable Long lineItemId,
            @Valid @RequestBody UpdateContractLineItemRequest request) {
        return ResponseEntity.ok(lineItemService.updateLineItem(contractId, lineItemId, request));
    }

    @DeleteMapping("/{lineItemId}")
    @Operation(summary = "Delete a line item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Line item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Line item or contract not found"),
            @ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<Void> deleteLineItem(
            @PathVariable Long contractId,
            @PathVariable Long lineItemId) {
        lineItemService.deleteLineItem(contractId, lineItemId);
        return ResponseEntity.noContent().build();
    }
}
