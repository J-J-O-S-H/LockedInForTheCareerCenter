package com.lockedin.careercenter.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "events")
@CompoundIndex(name = "priority_datetime_idx", def = "{'priority': 1, 'eventDateTime': 1}")
public class EventDocument {

    @Id
    private String id;

    private String title;

    private String description;

    private String location;

    @Indexed
    private Instant eventDateTime;

    private int maxVolunteers;

    private int currentVolunteers;

    @Indexed
    private EventPriority priority;

    @Indexed
    private EventStatus status = EventStatus.ACTIVE;

    private String createdByUserId;

    private Instant createdAt;

    private Instant updatedAt;

    public EventDocument() {
    }

    public EventDocument(
            String title,
            String description,
            String location,
            Instant eventDateTime,
            int maxVolunteers,
            int currentVolunteers,
            EventPriority priority,
            String createdByUserId,
            Instant createdAt,
            Instant updatedAt) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventDateTime = eventDateTime;
        this.maxVolunteers = maxVolunteers;
        this.currentVolunteers = currentVolunteers;
        this.priority = priority;
        this.status = EventStatus.ACTIVE;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Instant eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public int getMaxVolunteers() {
        return maxVolunteers;
    }

    public void setMaxVolunteers(int maxVolunteers) {
        this.maxVolunteers = maxVolunteers;
    }

    public int getCurrentVolunteers() {
        return currentVolunteers;
    }

    public void setCurrentVolunteers(int currentVolunteers) {
        this.currentVolunteers = currentVolunteers;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public void setPriority(EventPriority priority) {
        this.priority = priority;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
