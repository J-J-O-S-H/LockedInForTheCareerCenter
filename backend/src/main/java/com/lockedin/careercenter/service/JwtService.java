package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.security.JwtPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer("locked-in-career-center").build();
        this.expirationMs = expirationMs;
    }

    public String createToken(UserDocument user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);

        return JWT.create()
                .withIssuer("locked-in-career-center")
                .withSubject(user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }

    public JwtPrincipal verify(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getSubject();
            String email = jwt.getClaim("email").asString();
            String roleValue = jwt.getClaim("role").asString();

            if (userId == null || email == null || roleValue == null) {
                throw new IllegalArgumentException("Token is missing required claims.");
            }

            return new JwtPrincipal(userId, email, UserRole.valueOf(roleValue));
        } catch (JWTVerificationException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid or expired token.", ex);
        }
    }
}
