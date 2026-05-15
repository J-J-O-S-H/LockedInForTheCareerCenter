package com.lockedin.careercenter.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lockedin.careercenter.dto.ErrorResponse;
import com.lockedin.careercenter.dto.GoogleLoginRequest;
import com.lockedin.careercenter.dto.LoginResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
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

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            LoginResponse response = userService.loginWithGoogle(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            ErrorResponse error = new ErrorResponse(
                    "Unauthorized",
                    ex.getMessage(),
                    Map.of(),
                    Instant.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException ex) {
            ErrorResponse error = new ErrorResponse(
                    "Internal Server Error",
                    "Failed to process Google login.",
                    Map.of(),
                    Instant.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
