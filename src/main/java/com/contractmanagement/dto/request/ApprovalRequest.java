package com.contractmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApprovalRequest(
        @NotBlank(message = "Approver name is required")
        String approverName,
        String remarks
) {}
