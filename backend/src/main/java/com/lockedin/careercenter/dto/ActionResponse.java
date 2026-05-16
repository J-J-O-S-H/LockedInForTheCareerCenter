package com.lockedin.careercenter.dto;

import java.time.Instant;

public record ActionResponse(
        String message,
        Instant timestamp) {
}
