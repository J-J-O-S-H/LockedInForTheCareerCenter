package com.lockedin.careercenter.dto;

import java.time.Instant;

import com.lockedin.careercenter.model.EventRegistrationStatus;

public record EventRegistrationResponse(
        String id,
        String eventId,
        String userId,
        String userEmail,
        EventRegistrationStatus status,
        Instant registeredAt,
        Instant updatedAt,
        EventResponse event) {
}
