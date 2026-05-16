package com.lockedin.careercenter.repository;

import java.util.List;
import java.util.Optional;

import com.lockedin.careercenter.model.EventRegistrationDocument;
import com.lockedin.careercenter.model.EventRegistrationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRegistrationRepository extends MongoRepository<EventRegistrationDocument, String> {
    Optional<EventRegistrationDocument> findByEventIdAndUserId(String eventId, String userId);

    Optional<EventRegistrationDocument> findByEventIdAndUserIdAndStatus(
            String eventId,
            String userId,
            EventRegistrationStatus status);

    List<EventRegistrationDocument> findByUserIdAndStatusOrderByRegisteredAtDesc(
            String userId,
            EventRegistrationStatus status);
}
