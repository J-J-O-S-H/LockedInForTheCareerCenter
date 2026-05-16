package com.lockedin.careercenter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.lockedin.careercenter.config.GlobalExceptionHandler;
import com.lockedin.careercenter.config.SecurityConfig;
import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.EventRegistrationStatus;
import com.lockedin.careercenter.model.EventStatus;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.repository.UserRepository;
import com.lockedin.careercenter.security.JwtAuthenticationFilter;
import com.lockedin.careercenter.security.JwtPrincipal;
import com.lockedin.careercenter.service.EventRegistrationService;
import com.lockedin.careercenter.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserRegistrationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-with-enough-length-for-hmac",
        "app.jwt.expiration-ms=86400000"
})
class UserRegistrationControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventRegistrationService eventRegistrationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void currentUserRegistrationsReturnsAuthenticatedUsersRegistrations() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
        when(jwtService.verify("volunteer-token")).thenReturn(principal);
        when(eventRegistrationService.getCurrentUserRegistrations("volunteer-1"))
                .thenReturn(List.of(registrationResponse()));

        mockMvc.perform(get("/api/users/me/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer volunteer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("event-1"))
                .andExpect(jsonPath("$[0].userId").value("volunteer-1"))
                .andExpect(jsonPath("$[0].event.title").value("Career Fair"));
    }

    @Test
    void currentUserRegistrationsFailsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/users/me/registrations"))
                .andExpect(status().isUnauthorized());
    }

    private EventRegistrationResponse registrationResponse() {
        Instant now = Instant.now();
        return new EventRegistrationResponse(
                "registration-1",
                "event-1",
                "volunteer-1",
                "volunteer@example.com",
                EventRegistrationStatus.REGISTERED,
                now,
                now,
                new EventResponse(
                        "event-1",
                        "Career Fair",
                        "Meet employers.",
                        "Union Ballroom",
                        now.plusSeconds(3600),
                        10,
                        1,
                        9,
                        EventPriority.HIGH,
                        EventStatus.ACTIVE,
                        "admin-1",
                        now,
                        now));
    }
}
