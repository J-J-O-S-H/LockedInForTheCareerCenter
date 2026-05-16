package com.lockedin.careercenter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import com.lockedin.careercenter.config.GlobalExceptionHandler;
import com.lockedin.careercenter.config.SecurityConfig;
import com.lockedin.careercenter.dto.AuthResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.dto.UserRegistrationRequest;
import com.lockedin.careercenter.dto.UserResponse;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.repository.UserRepository;
import com.lockedin.careercenter.security.JwtAuthenticationFilter;
import com.lockedin.careercenter.service.JwtService;
import com.lockedin.careercenter.service.UserService;
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

@WebMvcTest(controllers = AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-with-enough-length-for-hmac",
        "app.jwt.expiration-ms=86400000"
})
class AuthControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void loginEndpointRemainsPublicAndReturnsToken() throws Exception {
        UserResponse user = userResponse();
        when(userService.login(any(UserLoginRequest.class)))
                .thenReturn(new AuthResponse(user, "Login successful.", "login-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void registerEndpointRemainsPublicAndReturnsToken() throws Exception {
        UserResponse user = userResponse();
        when(userService.register(any(UserRegistrationRequest.class)))
                .thenReturn(new AuthResponse(user, "Registration successful.", "registration-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Hornet",
                                  "role": "VOLUNTEER",
                                  "email": "jane@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("registration-token"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void currentUserSucceedsWithValidToken() throws Exception {
        UserDocument userDocument = userDocument();
        when(userService.getCurrentUser("user-1")).thenReturn(userResponse());

        String token = jwtService.createToken(userDocument);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void currentUserFailsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserFailsWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer definitely-not-valid"))
                .andExpect(status().isUnauthorized());
    }

    private UserResponse userResponse() {
        Instant now = Instant.now();
        return new UserResponse(
                "user-1",
                "Jane",
                "Hornet",
                "jane@example.com",
                UserRole.VOLUNTEER,
                now,
                now);
    }

    private UserDocument userDocument() {
        Instant now = Instant.now();
        UserDocument user = new UserDocument(
                "Jane",
                "Hornet",
                "jane@example.com",
                "$2a$10$notARealHashForThisTest",
                UserRole.VOLUNTEER,
                now,
                now);
        user.setId("user-1");
        return user;
    }
}
