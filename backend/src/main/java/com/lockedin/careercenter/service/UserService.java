package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lockedin.careercenter.dto.GoogleLoginRequest;
import com.lockedin.careercenter.dto.LoginResponse;
import com.lockedin.careercenter.dto.UserLoginRequest;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final String googleClientId;

    public UserService(UserRepository userRepository, @Value("${app.google.client-id:}") String googleClientId) {
        this.userRepository = userRepository;
        this.googleClientId = googleClientId;
    }

    public LoginResponse login(UserLoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return new LoginResponse(user.getEmail(), "Login successful.");
    }

    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            DecodedJWT jwt = JWT.decode(request.token());

            String audience = jwt.getAudience() != null && !jwt.getAudience().isEmpty()
                    ? jwt.getAudience().get(0)
                    : null;

            if (audience == null || !audience.equals(googleClientId)) {
                throw new IllegalArgumentException("Invalid Google token audience.");
            }

            String email = jwt.getClaim("email").asString();
            String googleId = jwt.getSubject();
            String displayName = jwt.getClaim("name").asString();
            String photoUrl = jwt.getClaim("picture").asString();

            if (email == null || googleId == null) {
                throw new IllegalArgumentException("Invalid Google token: missing email or subject.");
            }

            email = email.trim().toLowerCase(Locale.ROOT);

            UserDocument user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                user = new UserDocument(email, googleId, displayName, photoUrl, Instant.now());
            } else {
                user.setGoogleId(googleId);
                user.setDisplayName(displayName);
                user.setPhotoUrl(photoUrl);
                user.setAuthProvider("google");
            }

            userRepository.save(user);
            return new LoginResponse(user.getEmail(), "Login successful.");

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid Google token: " + ex.getMessage());
        }
    }

    public void seedInitialUser() {
        if (userRepository.count() == 0) {
            userRepository.save(new UserDocument(
                    "returninguser@example.com",
                    "Password123!",
                    Instant.now()));
        }
    }
}
