package com.lockedin.careercenter.dto;

import java.time.Instant;

import com.lockedin.careercenter.model.EventPriority;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventCreateRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Event date and time is required")
        @Future(message = "Event date and time must be in the future")
        Instant eventDateTime,

        @Min(value = 1, message = "Maximum volunteers must be greater than 0")
        int maxVolunteers,

        @NotNull(message = "Priority is required")
        EventPriority priority) {
}
