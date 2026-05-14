package com.lockedin.careercenter.dto;

import java.time.Instant;

public record ExampleResponse(
        String id,
        String title,
        String message,
        Instant createdAt) {
}
