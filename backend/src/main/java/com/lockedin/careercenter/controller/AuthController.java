package com.lockedin.careercenter.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lockedin.careercenter.dto.AuthResponse;
import com.lockedin.careercenter.dto.ErrorResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.dto.UserRegistrationRequest;
import com.lockedin.careercenter.dto.UserResponse;
import com.lockedin.careercenter.security.JwtPrincipal;
import com.lockedin.careercenter.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            AuthResponse response = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException ex) {
            String message = "An account with this email already exists.".equals(ex.getMessage())
                    ? "An account with this email already exists. Try logging in instead."
                    : ex.getMessage();
            ErrorResponse error = new ErrorResponse(
                    "Conflict",
                    message,
                    Map.of(),
                    Instant.now());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ErrorResponse error = new ErrorResponse(
                    "Unauthorized",
                    ex.getMessage(),
                    Map.of(),
                    Instant.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal JwtPrincipal principal) {
        try {
            UserResponse response = userService.getCurrentUser(principal.userId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ErrorResponse error = new ErrorResponse(
                    "Unauthorized",
                    ex.getMessage(),
                    Map.of(),
                    Instant.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
