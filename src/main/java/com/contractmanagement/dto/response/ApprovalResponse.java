package com.contractmanagement.dto.response;

import java.time.OffsetDateTime;

public record ApprovalResponse(
        Long id,
        Long contractId,
        String approverName,
        OffsetDateTime approvedAt,
        String remarks
) {}
