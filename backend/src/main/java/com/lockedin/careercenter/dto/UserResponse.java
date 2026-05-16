package com.lockedin.careercenter.dto;

import java.time.Instant;

import com.lockedin.careercenter.model.UserRole;

public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {
}
