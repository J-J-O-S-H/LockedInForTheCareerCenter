package com.lockedin.careercenter.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lockedin.careercenter.config.GlobalExceptionHandler;
import com.lockedin.careercenter.dto.UserRegistrationRequest;
import com.lockedin.careercenter.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerValidationTests {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        userService = mock(UserService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void rejectsInvalidRegistrationEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Hornet",
                                  "role": "VOLUNTEER",
                                  "email": "not-an-email",
                                  "password": "Password123!"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter a valid email address."))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void rejectsRegistrationPasswordWithoutSpecialCharacter() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Hornet",
                                  "role": "VOLUNTEER",
                                  "email": "jane@example.com",
                                  "password": "Password123"
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must include at least one special character."))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void duplicateRegistrationEmailReturnsFriendlyConflictMessage() throws Exception {
        when(userService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalStateException("An account with this email already exists."));

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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("An account with this email already exists. Try logging in instead."));
    }
}
