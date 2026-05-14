package com.lockedin.careercenter.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        String error,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp) {
}
