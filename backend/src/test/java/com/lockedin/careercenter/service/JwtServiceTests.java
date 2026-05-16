package com.lockedin.careercenter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.security.JwtPrincipal;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService(
            "test-secret-with-enough-length-for-hmac",
            86_400_000);

    @Test
    void tokenContainsIdentityAndRoleClaims() {
        UserDocument user = user();

        String token = jwtService.createToken(user);
        JwtPrincipal principal = jwtService.verify(token);

        assertEquals("user-1", principal.userId());
        assertEquals("jane@example.com", principal.email());
        assertEquals(UserRole.ADMIN, principal.role());
        assertTrue(principal.authorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void tokenDoesNotExposePasswordHash() {
        String token = jwtService.createToken(user());

        DecodedJWT decoded = JWT.decode(token);

        assertFalse(decoded.getClaims().containsKey("passwordHash"));
        assertFalse(decoded.getClaims().containsKey("password"));
    }

    private UserDocument user() {
        Instant now = Instant.now();
        UserDocument user = new UserDocument(
                "Jane",
                "Hornet",
                "jane@example.com",
                "$2a$10$notARealHashForThisTest",
                UserRole.ADMIN,
                now,
                now);
        user.setId("user-1");
        return user;
    }
}
