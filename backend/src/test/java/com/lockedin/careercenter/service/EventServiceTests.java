package com.lockedin.careercenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.lockedin.careercenter.dto.EventCreateRequest;
import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.EventStatus;
import com.lockedin.careercenter.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository);
    }

    @Test
    void returnsUpcomingEventsSortedByPriorityThenDateTime() {
        Instant now = Instant.now();
        EventDocument lowSoon = event("low-soon", "Low Soon", now.plusSeconds(3600), EventPriority.LOW, 5, 1);
        EventDocument highLater = event("high-later", "High Later", now.plusSeconds(7200), EventPriority.HIGH, 5, 1);
        EventDocument highSoon = event("high-soon", "High Soon", now.plusSeconds(1800), EventPriority.HIGH, 5, 1);

        when(eventRepository.findUpcomingActiveEvents(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(lowSoon, highLater, highSoon));

        List<EventResponse> events = eventService.getUpcomingEvents();

        assertEquals("high-soon", events.get(0).id());
        assertEquals("high-later", events.get(1).id());
        assertEquals("low-soon", events.get(2).id());
    }

    @Test
    void calculatesAvailableSpots() {
        Instant now = Instant.now();
        when(eventRepository.findUpcomingActiveEvents(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(event("event-1", "Career Fair", now.plusSeconds(3600), EventPriority.MEDIUM, 10, 4)));

        EventResponse response = eventService.getUpcomingEvents().get(0);

        assertEquals(6, response.availableSpots());
    }

    @Test
    void createsEventWithCurrentVolunteersDefaultedToZero() {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        EventCreateRequest request = new EventCreateRequest(
                "Career Fair",
                "Meet employers.",
                "Union Ballroom",
                eventDateTime,
                12,
                EventPriority.HIGH);

        when(eventRepository.save(any(EventDocument.class))).thenAnswer(invocation -> {
            EventDocument event = invocation.getArgument(0);
            event.setId("event-1");
            return event;
        });

        EventResponse response = eventService.createEvent(request, "admin-1");

        assertEquals("event-1", response.id());
        assertEquals("Career Fair", response.title());
        assertEquals(0, response.currentVolunteers());
        assertEquals(12, response.availableSpots());
        assertEquals(EventPriority.HIGH, response.priority());
        assertEquals(EventStatus.ACTIVE, response.status());
        assertEquals("admin-1", response.createdByUserId());
    }

    @Test
    void deletesEventByArchivingIt() {
        EventDocument event = event("event-1", "Career Fair", Instant.now().plusSeconds(3600), EventPriority.HIGH, 10, 1);
        when(eventRepository.findById("event-1")).thenReturn(java.util.Optional.of(event));
        when(eventRepository.save(any(EventDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.deleteEvent("event-1");

        assertEquals(EventStatus.DELETED, event.getStatus());
    }

    private EventDocument event(
            String id,
            String title,
            Instant eventDateTime,
            EventPriority priority,
            int maxVolunteers,
            int currentVolunteers) {
        Instant now = Instant.now();
        EventDocument event = new EventDocument(
                title,
                "Description",
                "Location",
                eventDateTime,
                maxVolunteers,
                currentVolunteers,
                priority,
                "admin-1",
                now,
                now);
        event.setId(id);
        return event;
    }
}
