package com.lockedin.careercenter.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "event_registrations")
@CompoundIndex(name = "unique_event_user_registration_idx", def = "{'eventId': 1, 'userId': 1}", unique = true)
public class EventRegistrationDocument {

    @Id
    private String id;

    @Indexed
    private String eventId;

    @Indexed
    private String userId;

    private String userEmail;

    private EventRegistrationStatus status;

    private Instant registeredAt;

    private Instant updatedAt;

    public EventRegistrationDocument() {
    }

    public EventRegistrationDocument(
            String eventId,
            String userId,
            String userEmail,
            EventRegistrationStatus status,
            Instant registeredAt,
            Instant updatedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.status = status;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public EventRegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(EventRegistrationStatus status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
