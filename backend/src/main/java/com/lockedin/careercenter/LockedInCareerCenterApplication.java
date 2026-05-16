package com.lockedin.careercenter;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.UserRepository;

@SpringBootApplication
public class LockedInCareerCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockedInCareerCenterApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                Instant now = Instant.now();
                userRepository.save(new UserDocument(
                        "Returning",
                        "User",
                        "returninguser@example.com",
                        passwordEncoder.encode("Password123!"),
                        UserRole.VOLUNTEER,
                        now,
                        now));
            }
        };
    }
}
