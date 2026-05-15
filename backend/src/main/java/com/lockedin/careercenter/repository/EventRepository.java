package com.lockedin.careercenter.repository;

import java.util.List;

import com.lockedin.careercenter.model.EventDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findAllByOrderByEventDateAsc(Pageable pageable);
}