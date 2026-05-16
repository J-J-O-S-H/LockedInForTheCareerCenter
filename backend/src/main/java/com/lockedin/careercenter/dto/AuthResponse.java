package com.lockedin.careercenter.dto;

public record AuthResponse(
        UserResponse user,
        String message,
        String token) {
}
