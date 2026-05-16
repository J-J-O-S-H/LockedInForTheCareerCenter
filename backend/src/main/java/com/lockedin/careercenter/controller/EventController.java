package com.lockedin.careercenter.controller;

import java.util.List;

import com.lockedin.careercenter.dto.ActionResponse;
import com.lockedin.careercenter.dto.EventCreateRequest;
import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.security.JwtPrincipal;
import com.lockedin.careercenter.service.EventRegistrationService;
import com.lockedin.careercenter.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService eventRegistrationService;

    public EventController(
            EventService eventService,
            EventRegistrationService eventRegistrationService) {
        this.eventService = eventService;
        this.eventRegistrationService = eventRegistrationService;
    }

    @GetMapping
    public List<EventResponse> getEvents() {
        return eventService.getUpcomingEvents();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return eventService.createEvent(request, principal.userId());
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ActionResponse deleteEvent(@PathVariable String eventId) {
        return eventService.deleteEvent(eventId);
    }

    @PostMapping("/{eventId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public EventRegistrationResponse registerForEvent(
            @PathVariable String eventId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return eventRegistrationService.registerForEvent(eventId, principal);
    }

    @DeleteMapping("/{eventId}/registrations/me")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public ActionResponse withdrawFromEvent(
            @PathVariable String eventId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return eventRegistrationService.withdrawFromEvent(eventId, principal);
    }
}
