package com.contractmanagement.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<ErrorDetail> details,
        OffsetDateTime timestamp
) {
    public record ErrorDetail(String field, String issue) {}
}
