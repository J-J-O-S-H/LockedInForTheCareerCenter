package com.lockedin.careercenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import com.lockedin.careercenter.dto.AuthResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.dto.UserRegistrationRequest;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registersUserSuccessfully() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Jane",
                "Hornet",
                UserRole.VOLUNTEER,
                "JANE@example.com",
                "Password123!");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });
        when(jwtService.createToken(any(UserDocument.class))).thenReturn("registration-token");

        AuthResponse response = userService.register(request);

        assertEquals("user-1", response.user().id());
        assertEquals("Jane", response.user().firstName());
        assertEquals("Hornet", response.user().lastName());
        assertEquals("jane@example.com", response.user().email());
        assertEquals(UserRole.VOLUNTEER, response.user().role());
        assertEquals("registration-token", response.token());
    }

    @Test
    void rejectsDuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Jane",
                "Hornet",
                UserRole.VOLUNTEER,
                "jane@example.com",
                "Password123!");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.register(request));
    }

    @Test
    void logsInSuccessfully() {
        Instant now = Instant.now();
        UserDocument user = new UserDocument(
                "Jane",
                "Hornet",
                "jane@example.com",
                passwordEncoder.encode("Password123!"),
                UserRole.VOLUNTEER,
                now,
                now);
        user.setId("user-1");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtService.createToken(user)).thenReturn("login-token");

        AuthResponse response = userService.login(new UserLoginRequest("JANE@example.com", "Password123!"));

        assertEquals("user-1", response.user().id());
        assertEquals("jane@example.com", response.user().email());
        assertEquals("Login successful.", response.message());
        assertEquals("login-token", response.token());
    }

    @Test
    void rejectsWrongPassword() {
        Instant now = Instant.now();
        UserDocument user = new UserDocument(
                "Jane",
                "Hornet",
                "jane@example.com",
                passwordEncoder.encode("Password123!"),
                UserRole.VOLUNTEER,
                now,
                now);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(new UserLoginRequest("jane@example.com", "Wrong123!")));
    }

    @Test
    void doesNotStorePasswordAsPlaintext() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Jane",
                "Hornet",
                UserRole.VOLUNTEER,
                "jane@example.com",
                "Password123!");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(UserDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<UserDocument> userCaptor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository).save(userCaptor.capture());

        String storedHash = userCaptor.getValue().getPasswordHash();
        assertNotEquals("Password123!", storedHash);
        assertTrue(passwordEncoder.matches("Password123!", storedHash));
    }
}
