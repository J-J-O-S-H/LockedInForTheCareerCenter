package com.lockedin.careercenter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import com.lockedin.careercenter.config.CorsConfig;
import com.lockedin.careercenter.config.GlobalExceptionHandler;
import com.lockedin.careercenter.config.SecurityConfig;
import com.lockedin.careercenter.dto.ActionResponse;
import com.lockedin.careercenter.dto.EventCreateRequest;
import com.lockedin.careercenter.dto.EventRegistrationResponse;
import com.lockedin.careercenter.dto.EventResponse;
import com.lockedin.careercenter.model.EventRegistrationStatus;
import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.EventStatus;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.repository.UserRepository;
import com.lockedin.careercenter.security.JwtAuthenticationFilter;
import com.lockedin.careercenter.security.JwtPrincipal;
import com.lockedin.careercenter.service.EventRegistrationService;
import com.lockedin.careercenter.service.EventService;
import com.lockedin.careercenter.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventController.class)
@Import({
        CorsConfig.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "app.jwt.secret=test-secret-with-enough-length-for-hmac",
        "app.jwt.expiration-ms=86400000"
})
class EventControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

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
    void getEventsIsPublicAndReturnsRequiredFields() throws Exception {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        when(eventService.getUpcomingEvents()).thenReturn(List.of(eventResponse(eventDateTime)));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Career Fair"))
                .andExpect(jsonPath("$[0].description").value("Meet employers."))
                .andExpect(jsonPath("$[0].location").value("Union Ballroom"))
                .andExpect(jsonPath("$[0].eventDateTime").exists())
                .andExpect(jsonPath("$[0].availableSpots").value(9))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void adminCanCreateEvent() throws Exception {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);
        when(eventService.createEvent(any(EventCreateRequest.class), eq("admin-1")))
                .thenReturn(eventResponse(eventDateTime));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(eventDateTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Career Fair"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createEventFailsWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(Instant.now().plusSeconds(3600))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEventFailsForNonAdminRole() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
        when(jwtService.verify("volunteer-token")).thenReturn(principal);

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer volunteer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(Instant.now().plusSeconds(3600))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEventValidationRejectsInvalidFields() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "",
                                  "location": "",
                                  "eventDateTime": "2020-01-01T10:00:00Z",
                                  "maxVolunteers": 0,
                                  "priority": null
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please complete all required fields."))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.location").exists())
                .andExpect(jsonPath("$.fieldErrors.eventDateTime").exists())
                .andExpect(jsonPath("$.fieldErrors.maxVolunteers").exists())
                .andExpect(jsonPath("$.fieldErrors.priority").exists());
    }

    @Test
    void createEventValidationRejectsInvalidPriorityWithSafeMessage() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Career Fair",
                                  "description": "Meet employers.",
                                  "location": "Union Ballroom",
                                  "eventDateTime": "%s",
                                  "maxVolunteers": 10,
                                  "priority": "URGENT"
                                }
                                """.formatted(Instant.now().plusSeconds(3600))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Priority must be HIGH, MEDIUM, or LOW."))
                .andExpect(jsonPath("$.fieldErrors.priority").exists());
    }

    @Test
    void adminCanDeleteEvent() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);
        when(eventService.deleteEvent("event-1"))
                .thenReturn(new ActionResponse("Event deleted.", Instant.now()));

        mockMvc.perform(delete("/api/events/event-1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted."));
    }

    @Test
    void deleteEventFailsWithoutJwt() throws Exception {
        mockMvc.perform(delete("/api/events/event-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteEventFailsForNonAdminRole() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
        when(jwtService.verify("volunteer-token")).thenReturn(principal);

        mockMvc.perform(delete("/api/events/event-1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer volunteer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEventPreflightReturnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/events/event-1")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("DELETE")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void volunteerCanRegisterForEvent() throws Exception {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        JwtPrincipal principal = new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
        when(jwtService.verify("volunteer-token")).thenReturn(principal);
        when(eventRegistrationService.registerForEvent("event-1", principal))
                .thenReturn(registrationResponse(eventDateTime));

        mockMvc.perform(post("/api/events/event-1/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer volunteer-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("event-1"))
                .andExpect(jsonPath("$.userId").value("volunteer-1"))
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.event.currentVolunteers").value(2))
                .andExpect(jsonPath("$.event.availableSpots").value(8));
    }

    @Test
    void adminCanRegisterForEvent() throws Exception {
        Instant eventDateTime = Instant.now().plusSeconds(3600);
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);
        when(eventRegistrationService.registerForEvent("event-1", principal))
                .thenReturn(registrationResponse(
                        eventDateTime,
                        "admin-1",
                        "admin@example.com"));

        mockMvc.perform(post("/api/events/event-1/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("event-1"))
                .andExpect(jsonPath("$.userId").value("admin-1"))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void registerForEventFailsWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/events/event-1/registrations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerForEventFailsForStudentRole() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("student-1", "student@example.com", UserRole.STUDENT);
        when(jwtService.verify("student-token")).thenReturn(principal);

        mockMvc.perform(post("/api/events/event-1/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer student-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerForEventFailsForEmployerRole() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("employer-1", "employer@example.com", UserRole.EMPLOYER);
        when(jwtService.verify("employer-token")).thenReturn(principal);

        mockMvc.perform(post("/api/events/event-1/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer employer-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void volunteerCanWithdrawFromEvent() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("volunteer-1", "volunteer@example.com", UserRole.VOLUNTEER);
        when(jwtService.verify("volunteer-token")).thenReturn(principal);
        when(eventRegistrationService.withdrawFromEvent("event-1", principal))
                .thenReturn(new ActionResponse("Registration withdrawn.", Instant.now()));

        mockMvc.perform(delete("/api/events/event-1/registrations/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer volunteer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration withdrawn."));
    }

    @Test
    void adminCanWithdrawFromEvent() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("admin-1", "admin@example.com", UserRole.ADMIN);
        when(jwtService.verify("admin-token")).thenReturn(principal);
        when(eventRegistrationService.withdrawFromEvent("event-1", principal))
                .thenReturn(new ActionResponse("Registration withdrawn.", Instant.now()));

        mockMvc.perform(delete("/api/events/event-1/registrations/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration withdrawn."));
    }

    @Test
    void withdrawFromEventFailsWithoutJwt() throws Exception {
        mockMvc.perform(delete("/api/events/event-1/registrations/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withdrawFromEventFailsForStudentRole() throws Exception {
        JwtPrincipal principal = new JwtPrincipal("student-1", "student@example.com", UserRole.STUDENT);
        when(jwtService.verify("student-token")).thenReturn(principal);

        mockMvc.perform(delete("/api/events/event-1/registrations/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer student-token"))
                .andExpect(status().isForbidden());
    }

    private EventResponse eventResponse(Instant eventDateTime) {
        Instant now = Instant.now();
        return new EventResponse(
                "event-1",
                "Career Fair",
                "Meet employers.",
                "Union Ballroom",
                eventDateTime,
                10,
                1,
                9,
                EventPriority.HIGH,
                EventStatus.ACTIVE,
                "admin-1",
                now,
                now);
    }

    private EventRegistrationResponse registrationResponse(Instant eventDateTime) {
        return registrationResponse(eventDateTime, "volunteer-1", "volunteer@example.com");
    }

    private EventRegistrationResponse registrationResponse(
            Instant eventDateTime,
            String userId,
            String userEmail) {
        Instant now = Instant.now();
        return new EventRegistrationResponse(
                "registration-1",
                "event-1",
                userId,
                userEmail,
                EventRegistrationStatus.REGISTERED,
                now,
                now,
                new EventResponse(
                        "event-1",
                        "Career Fair",
                        "Meet employers.",
                        "Union Ballroom",
                        eventDateTime,
                        10,
                        2,
                        8,
                        EventPriority.HIGH,
                        EventStatus.ACTIVE,
                        "admin-1",
                        now,
                        now));
    }

    private String validRequestJson(Instant eventDateTime) {
        return """
                {
                  "title": "Career Fair",
                  "description": "Meet employers.",
                  "location": "Union Ballroom",
                  "eventDateTime": "%s",
                  "maxVolunteers": 10,
                  "priority": "HIGH"
                }
                """.formatted(eventDateTime);
    }
}
