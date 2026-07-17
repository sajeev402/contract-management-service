package com.contractmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TerminateRequest(
        @NotBlank(message = "Termination reason is mandatory")
        String terminationReason
) {}
