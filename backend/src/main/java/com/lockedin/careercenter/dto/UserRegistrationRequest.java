package com.lockedin.careercenter.dto;

import com.lockedin.careercenter.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull(message = "Role is required")
        UserRole role,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = ".*[^A-Za-z0-9].*",
                message = "Password must contain at least one special character")
        String password) {
}
