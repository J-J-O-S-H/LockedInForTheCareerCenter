package com.lockedin.careercenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.exception.ApiException;
import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.EventRegistrationDocument;
import com.lockedin.careercenter.model.EventRegistrationStatus;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.EventRegistrationRepository;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.security.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository registrationRepository;

    private EventRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new EventRegistrationService(
                eventRepository,
                registrationRepository,
                new EventService(eventRepository));
    }

    @Test
    void volunteerCanRegisterForEventAndCurrentVolunteersIncrements() {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        EventDocument originalEvent = event("event-1", eventDateTime, 3, 1);
        EventDocument updatedEvent = event("event-1", eventDateTime, 3, 2);

        when(eventRepository.findById("event-1"))
                .thenReturn(Optional.of(originalEvent), Optional.of(updatedEvent));
        when(registrationRepository.findByEventIdAndUserId("event-1", "volunteer-1")).thenReturn(Optional.empty());
        when(registrationRepository.save(any(EventRegistrationDocument.class))).thenAnswer(invocation -> {
            EventRegistrationDocument registration = invocation.getArgument(0);
            registration.setId("registration-1");
            return registration;
        });
        when(eventRepository.incrementCurrentVolunteersIfCapacityAvailable(
                eq("event-1"),
                any(Instant.class),
                eq(3),
                any(Instant.class))).thenReturn(1L);

        EventRegistrationResponse response = registrationService.registerForEvent("event-1", principal());

        assertEquals("registration-1", response.id());
        assertEquals("event-1", response.eventId());
        assertEquals("volunteer-1", response.userId());
        assertEquals(EventRegistrationStatus.REGISTERED, response.status());
        assertEquals(2, response.event().currentVolunteers());
        assertEquals(1, response.event().availableSpots());
    }

    @Test
    void registeringForNonexistentEventFails() {
        when(eventRepository.findById("missing-event")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.registerForEvent("missing-event", principal()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(registrationRepository, never()).save(any(EventRegistrationDocument.class));
    }

    @Test
    void registeringForFullEventFailsAndRegistrationIsRemoved() {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        EventDocument fullEvent = event("event-1", eventDateTime, 1, 1);
        EventRegistrationDocument savedRegistration = registration("registration-1", "event-1", "volunteer-1");

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(fullEvent));
        when(registrationRepository.findByEventIdAndUserId("event-1", "volunteer-1")).thenReturn(Optional.empty());
        when(registrationRepository.save(any(EventRegistrationDocument.class))).thenReturn(savedRegistration);
        when(eventRepository.incrementCurrentVolunteersIfCapacityAvailable(
                eq("event-1"),
                any(Instant.class),
                eq(1),
                any(Instant.class))).thenReturn(0L);

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.registerForEvent("event-1", principal()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("This event is full.", exception.getMessage());
        verify(registrationRepository).delete(savedRegistration);
    }

    @Test
    void duplicateRegistrationFailsBeforeCapacityIncrement() {
        EventDocument event = event("event-1", Instant.now().plusSeconds(3600), 5, 1);
        EventRegistrationDocument registration = registration("registration-1", "event-1", "volunteer-1");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId("event-1", "volunteer-1"))
                .thenReturn(Optional.of(registration));

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.registerForEvent("event-1", principal()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(registrationRepository, never()).save(any(EventRegistrationDocument.class));
        verify(eventRepository, never()).incrementCurrentVolunteersIfCapacityAvailable(
                any(),
                any(),
                any(Integer.class),
                any());
    }

    @Test
    void userCanRegisterAgainAfterWithdrawal() {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        EventDocument originalEvent = event("event-1", eventDateTime, 3, 1);
        EventDocument updatedEvent = event("event-1", eventDateTime, 3, 2);
        EventRegistrationDocument cancelledRegistration = registration("registration-1", "event-1", "volunteer-1");
        cancelledRegistration.setStatus(EventRegistrationStatus.CANCELLED);

        when(eventRepository.findById("event-1"))
                .thenReturn(Optional.of(originalEvent), Optional.of(updatedEvent));
        when(registrationRepository.findByEventIdAndUserId("event-1", "volunteer-1"))
                .thenReturn(Optional.of(cancelledRegistration));
        when(eventRepository.incrementCurrentVolunteersIfCapacityAvailable(
                eq("event-1"),
                any(Instant.class),
                eq(3),
                any(Instant.class))).thenReturn(1L);
        when(registrationRepository.save(cancelledRegistration)).thenReturn(cancelledRegistration);

        EventRegistrationResponse response = registrationService.registerForEvent("event-1", principal());

        assertEquals(EventRegistrationStatus.REGISTERED, response.status());
        assertEquals(2, response.event().currentVolunteers());
    }

    @Test
    void registeringForPastEventFails() {
        EventDocument event = event("event-1", Instant.now().minusSeconds(60), 5, 1);
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.registerForEvent("event-1", principal()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(registrationRepository, never()).save(any(EventRegistrationDocument.class));
    }

    @Test
    void currentUserRegistrationsReturnsOnlyRequestedUsersRegistrations() {
        EventRegistrationDocument registration = registration("registration-1", "event-1", "volunteer-1");
        EventDocument event = event("event-1", Instant.now().plusSeconds(3600), 5, 2);

        when(registrationRepository.findByUserIdAndStatusOrderByRegisteredAtDesc(
                "volunteer-1",
                EventRegistrationStatus.REGISTERED)).thenReturn(List.of(registration));
        when(eventRepository.findAllById(List.of("event-1"))).thenReturn(List.of(event));

        List<EventRegistrationResponse> registrations =
                registrationService.getCurrentUserRegistrations("volunteer-1");

        assertEquals(1, registrations.size());
        assertEquals("volunteer-1", registrations.get(0).userId());
        assertEquals("event-1", registrations.get(0).event().id());
        assertEquals(3, registrations.get(0).event().availableSpots());
    }

    @Test
    void volunteerCanWithdrawFromEventAndCurrentVolunteersDecrements() {
        EventDocument event = event("event-1", Instant.now().plusSeconds(3600), 5, 1);
        EventRegistrationDocument registration = registration("registration-1", "event-1", "volunteer-1");

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserIdAndStatus(
                "event-1",
                "volunteer-1",
                EventRegistrationStatus.REGISTERED)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(registration)).thenReturn(registration);
        when(eventRepository.decrementCurrentVolunteersIfPositive(eq("event-1"), any(Instant.class))).thenReturn(1L);

        registrationService.withdrawFromEvent("event-1", principal());

        assertEquals(EventRegistrationStatus.CANCELLED, registration.getStatus());
        verify(eventRepository).decrementCurrentVolunteersIfPositive(eq("event-1"), any(Instant.class));
    }

    @Test
    void withdrawalDoesNotDecrementBelowZero() {
        EventDocument event = event("event-1", Instant.now().plusSeconds(3600), 5, 0);
        EventRegistrationDocument registration = registration("registration-1", "event-1", "volunteer-1");

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserIdAndStatus(
                "event-1",
                "volunteer-1",
                EventRegistrationStatus.REGISTERED)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(registration)).thenReturn(registration);
        when(eventRepository.decrementCurrentVolunteersIfPositive(eq("event-1"), any(Instant.class))).thenReturn(0L);

        registrationService.withdrawFromEvent("event-1", principal());

        assertEquals(EventRegistrationStatus.CANCELLED, registration.getStatus());
        verify(eventRepository).decrementCurrentVolunteersIfPositive(eq("event-1"), any(Instant.class));
    }

    @Test
    void withdrawingFromEventWithoutActiveRegistrationFails() {
        EventDocument event = event("event-1", Instant.now().plusSeconds(3600), 5, 1);

        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserIdAndStatus(
                "event-1",
                "volunteer-1",
                EventRegistrationStatus.REGISTERED)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.withdrawFromEvent("event-1", principal()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(eventRepository, never()).decrementCurrentVolunteersIfPositive(any(), any());
    }

    @Test
    void withdrawingFromNonexistentEventFails() {
        when(eventRepository.findById("missing-event")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> registrationService.withdrawFromEvent("missing-event", principal()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    private JwtPrincipal principal() {
        return new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
    }

    private EventDocument event(String id, Instant eventDateTime, int maxVolunteers, int currentVolunteers) {
        Instant now = Instant.now();
        EventDocument event = new EventDocument(
                "Career Fair",
                "Meet employers.",
                "Union Ballroom",
                eventDateTime,
                maxVolunteers,
                currentVolunteers,
                EventPriority.HIGH,
                "admin-1",
                now,
                now);
        event.setId(id);
        return event;
    }

    private EventRegistrationDocument registration(String id, String eventId, String userId) {
        Instant now = Instant.now();
        EventRegistrationDocument registration = new EventRegistrationDocument(
                eventId,
                userId,
                "volunteer@example.com",
                EventRegistrationStatus.REGISTERED,
                now,
                now);
        registration.setId(id);
        return registration;
    }
}
