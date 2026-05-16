package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.lockedin.careercenter.dto.EventCreateRequest;
import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.dto.ActionResponse;
import com.lockedin.careercenter.exception.ApiException;
import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.model.EventStatus;
import com.lockedin.careercenter.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private static final int EVENT_LIST_LIMIT = 100;

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getUpcomingEvents() {
        return eventRepository
                .findUpcomingActiveEvents(
                        Instant.now(),
                        PageRequest.of(0, EVENT_LIST_LIMIT, Sort.by(Sort.Direction.ASC, "eventDateTime")))
                .stream()
                .sorted(Comparator
                        .comparingInt((EventDocument event) -> event.getPriority().getSortOrder())
                        .thenComparing(EventDocument::getEventDateTime))
                .map(this::toResponse)
                .toList();
    }

    public EventResponse createEvent(EventCreateRequest request, String createdByUserId) {
        Instant now = Instant.now();
        EventDocument event = new EventDocument(
                request.title().trim(),
                request.description().trim(),
                request.location().trim(),
                request.eventDateTime(),
                request.maxVolunteers(),
                0,
                request.priority(),
                createdByUserId,
                now,
                now);

        return toResponse(eventRepository.save(event));
    }

    public ActionResponse deleteEvent(String eventId) {
        EventDocument event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event was not found."));

        if (eventStatus(event) == EventStatus.DELETED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Event was not found.");
        }

        event.setStatus(EventStatus.DELETED);
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);

        return new ActionResponse("Event deleted.", Instant.now());
    }

    public EventResponse toResponse(EventDocument event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDateTime(),
                event.getMaxVolunteers(),
                event.getCurrentVolunteers(),
                Math.max(0, event.getMaxVolunteers() - event.getCurrentVolunteers()),
                event.getPriority(),
                eventStatus(event),
                event.getCreatedByUserId(),
                event.getCreatedAt(),
                event.getUpdatedAt());
    }

    private EventStatus eventStatus(EventDocument event) {
        return event.getStatus() == null ? EventStatus.ACTIVE : event.getStatus();
    }
}
