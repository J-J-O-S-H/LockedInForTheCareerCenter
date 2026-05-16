package com.lockedin.careercenter.controller;

import java.util.List;

import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.security.JwtPrincipal;
import com.lockedin.careercenter.service.EventRegistrationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/registrations")
public class UserRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    public UserRegistrationController(EventRegistrationService eventRegistrationService) {
        this.eventRegistrationService = eventRegistrationService;
    }

    @GetMapping
    public List<EventRegistrationResponse> getCurrentUserRegistrations(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return eventRegistrationService.getCurrentUserRegistrations(principal.userId());
    }
}
