package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lockedin.careercenter.dto.AuthResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.dto.UserRegistrationRequest;
import com.lockedin.careercenter.dto.UserResponse;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(UserRegistrationRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        Instant now = Instant.now();
        UserDocument user = new UserDocument(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role(),
                now,
                now);

        UserDocument savedUser = userRepository.save(user);
        return new AuthResponse(toResponse(savedUser), "Registration successful.", jwtService.createToken(savedUser));
    }

    public AuthResponse login(UserLoginRequest request) {
        String email = normalizeEmail(request.email());

        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return new AuthResponse(toResponse(user), "Login successful.", jwtService.createToken(user));
    }

    public UserResponse getCurrentUser(String userId) {
        UserDocument user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user no longer exists."));

        return toResponse(user);
    }

    public UserResponse toResponse(UserDocument user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
