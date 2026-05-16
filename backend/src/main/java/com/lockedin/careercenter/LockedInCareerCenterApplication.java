package com.lockedin.careercenter;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lockedin.careercenter.model.EventDocument;
import com.lockedin.careercenter.model.EventPriority;
import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.model.UserRole;
import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.repository.UserRepository;

@SpringBootApplication
public class LockedInCareerCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockedInCareerCenterApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDefaultData(
            UserRepository userRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder) {
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
                userRepository.save(new UserDocument(
                        "Admin",
                        "User",
                        "admin@example.com",
                        passwordEncoder.encode("Admin123!"),
                        UserRole.ADMIN,
                        now,
                        now));
            }

            if (eventRepository.count() == 0) {
                Instant now = Instant.now();
                eventRepository.save(new EventDocument(
                        "Spring Career Fair",
                        "Meet employers from technology, healthcare, government, and business fields.",
                        "University Union Ballroom",
                        now.plusSeconds(14L * 24 * 60 * 60),
                        20,
                        0,
                        EventPriority.HIGH,
                        "system",
                        now,
                        now));
                eventRepository.save(new EventDocument(
                        "Resume Review Workshop",
                        "Career Center staff will help students polish resumes before the recruiting season.",
                        "Career Center Lab",
                        now.plusSeconds(7L * 24 * 60 * 60),
                        8,
                        0,
                        EventPriority.MEDIUM,
                        "system",
                        now,
                        now));
                eventRepository.save(new EventDocument(
                        "Employer Networking Night",
                        "An evening networking event connecting students with local employers and alumni.",
                        "Alumni Center",
                        now.plusSeconds(21L * 24 * 60 * 60),
                        12,
                        0,
                        EventPriority.LOW,
                        "system",
                        now,
                        now));
            }
        };
    }
}
