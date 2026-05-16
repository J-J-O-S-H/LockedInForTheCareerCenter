package com.lockedin.careercenter.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lockedin.careercenter.dto.ErrorResponse;
import com.lockedin.careercenter.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return new ErrorResponse(
                "Validation failed",
                "Please check the request body.",
                errors,
                Instant.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody() {
        return new ErrorResponse(
                "Invalid request body",
                "Send valid JSON with the expected fields.",
                Map.of(),
                Instant.now());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(new ErrorResponse(
                        exception.getStatus().getReasonPhrase(),
                        exception.getMessage(),
                        Map.of(),
                        Instant.now()));
    }
}
