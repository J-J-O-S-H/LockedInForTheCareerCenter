package com.lockedin.careercenter.dto;

import java.time.Instant;

import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.EventStatus;

public record EventResponse(
        String id,
        String title,
        String description,
        String location,
        Instant eventDateTime,
        int maxVolunteers,
        int currentVolunteers,
        int availableSpots,
        EventPriority priority,
        EventStatus status,
        String createdByUserId,
        Instant createdAt,
        Instant updatedAt) {
}
