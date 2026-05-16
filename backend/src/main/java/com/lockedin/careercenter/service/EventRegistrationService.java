package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lockedin.careercenter.dto.ActionResponse;
import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.exception.ApiException;
import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.model.EventRegistrationDocument;
import com.lockedin.careercenter.model.EventRegistrationStatus;
import com.lockedin.careercenter.model.EventStatus;
import com.lockedin.careercenter.repository.EventRegistrationRepository;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.security.JwtPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EventRegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventService eventService;

    public EventRegistrationService(
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            EventService eventService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
    }

    public EventRegistrationResponse registerForEvent(String eventId, JwtPrincipal principal) {
        Instant now = Instant.now();
        EventDocument event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event was not found."));

        if (eventStatus(event) == EventStatus.DELETED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Event was not found.");
        }

        if (event.getEventDateTime().isBefore(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Registration is closed for past events.");
        }

        EventRegistrationDocument existingRegistration = registrationRepository
                .findByEventIdAndUserId(eventId, principal.userId())
                .orElse(null);

        if (existingRegistration != null && existingRegistration.getStatus() == EventRegistrationStatus.REGISTERED) {
            throw new ApiException(HttpStatus.CONFLICT, "You are already registered for this event.");
        }

        EventRegistrationDocument savedRegistration = existingRegistration == null
                ? saveRegistration(eventId, principal, now)
                : existingRegistration;

        long updatedEvents = eventRepository.incrementCurrentVolunteersIfCapacityAvailable(
                eventId,
                now,
                event.getMaxVolunteers(),
                now);

        if (updatedEvents == 0) {
            if (existingRegistration == null) {
                registrationRepository.delete(savedRegistration);
            }
            throw new ApiException(HttpStatus.CONFLICT, "This event is full.");
        }

        if (existingRegistration != null) {
            savedRegistration = reactivateRegistration(existingRegistration, principal.email(), now);
        }

        EventDocument updatedEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event was not found."));

        return toResponse(savedRegistration, updatedEvent);
    }

    public List<EventRegistrationResponse> getCurrentUserRegistrations(String userId) {
        List<EventRegistrationDocument> registrations =
                registrationRepository.findByUserIdAndStatusOrderByRegisteredAtDesc(
                        userId,
                        EventRegistrationStatus.REGISTERED);

        Map<String, EventDocument> eventsById = eventRepository
                .findAllById(registrations.stream().map(EventRegistrationDocument::getEventId).toList())
                .stream()
                .collect(Collectors.toMap(EventDocument::getId, Function.identity()));

        return registrations.stream()
                .filter(registration -> eventsById.containsKey(registration.getEventId()))
                .map(registration -> toResponse(registration, eventsById.get(registration.getEventId())))
                .toList();
    }

    public ActionResponse withdrawFromEvent(String eventId, JwtPrincipal principal) {
        Instant now = Instant.now();
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event was not found."));

        EventRegistrationDocument registration = registrationRepository
                .findByEventIdAndUserIdAndStatus(
                        eventId,
                        principal.userId(),
                        EventRegistrationStatus.REGISTERED)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.CONFLICT,
                        "You do not have an active registration for this event."));

        registration.setStatus(EventRegistrationStatus.CANCELLED);
        registration.setUpdatedAt(now);
        registrationRepository.save(registration);
        eventRepository.decrementCurrentVolunteersIfPositive(eventId, now);

        return new ActionResponse("Registration withdrawn.", Instant.now());
    }

    private EventRegistrationDocument saveRegistration(String eventId, JwtPrincipal principal, Instant now) {
        try {
            return registrationRepository.save(new EventRegistrationDocument(
                    eventId,
                    principal.userId(),
                    principal.email(),
                    EventRegistrationStatus.REGISTERED,
                    now,
                    now));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "You are already registered for this event.");
        }
    }

    private EventRegistrationDocument reactivateRegistration(
            EventRegistrationDocument registration,
            String userEmail,
            Instant now) {
        registration.setUserEmail(userEmail);
        registration.setStatus(EventRegistrationStatus.REGISTERED);
        registration.setRegisteredAt(now);
        registration.setUpdatedAt(now);
        return registrationRepository.save(registration);
    }

    private EventRegistrationResponse toResponse(
            EventRegistrationDocument registration,
            EventDocument event) {
        return new EventRegistrationResponse(
                registration.getId(),
                registration.getEventId(),
                registration.getUserId(),
                registration.getUserEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getUpdatedAt(),
                eventService.toResponse(event));
    }

    private EventStatus eventStatus(EventDocument event) {
        return event.getStatus() == null ? EventStatus.ACTIVE : event.getStatus();
    }
}
