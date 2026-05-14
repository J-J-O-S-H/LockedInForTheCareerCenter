package com.lockedin.careercenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExampleCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 80, message = "Title must be 80 characters or fewer")
        String title,

        @NotBlank(message = "Message is required")
        @Size(max = 500, message = "Message must be 500 characters or fewer")
        String message) {
}
