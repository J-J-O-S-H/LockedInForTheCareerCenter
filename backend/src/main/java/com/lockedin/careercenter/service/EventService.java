package com.lockedin.careercenter.service;

import java.util.List;

import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.repository.EventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> getEvents() {
        return eventRepository
                .findAllByOrderByEventDateAsc(PageRequest.of(0, 20))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(EventDocument event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getLocation(),
                event.getMaxVolunteers(),
                event.getCurrentVolunteers(),
                event.getMaxVolunteers() - event.getCurrentVolunteers(),
                event.getEventDate());
    }

    public EventResponse registerVolunteer(String eventId) {
        EventDocument event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        boolean loggedIn = true;

        if (!loggedIn) {
            throw new RuntimeException("Volunteer must be logged in");
        }

        if (event.getCurrentVolunteers() >= event.getMaxVolunteers()) {
            throw new RuntimeException("Event is full");
        }

        event.setCurrentVolunteers(event.getCurrentVolunteers() + 1);

        return toResponse(eventRepository.save(event));
    }
}
