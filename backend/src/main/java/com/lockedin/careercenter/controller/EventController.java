package com.lockedin.careercenter.controller;

import java.util.List;

import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> getEvents() {
        return eventService.getEvents();
    }

    @PostMapping("/{eventId}/register")
    public EventResponse registerVolunteer(@PathVariable String eventId) {
        return eventService.registerVolunteer(eventId);
    }
}