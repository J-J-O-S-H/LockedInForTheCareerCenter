package com.lockedin.careercenter;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.lockedin.careercenter.model.UserDocument;
import com.lockedin.careercenter.repository.UserRepository;

@SpringBootApplication
public class LockedInCareerCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockedInCareerCenterApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDefaultUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new UserDocument(
                        "returninguser@example.com",
                        "Password123!",
                        Instant.now()));
            }
        };
    }
}
