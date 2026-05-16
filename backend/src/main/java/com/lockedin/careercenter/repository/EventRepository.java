package com.lockedin.careercenter.repository;

import java.time.Instant;
import java.util.List;

import com.lockedin.careercenter.model.EventDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface EventRepository extends MongoRepository<EventDocument, String> {
    @Query("{ 'eventDateTime': { '$gte': ?0 }, '$or': [ { 'status': 'ACTIVE' }, { 'status': null } ] }")
    List<EventDocument> findUpcomingActiveEvents(Instant eventDateTime, Pageable pageable);

    @Query("{ '_id': ?0, 'eventDateTime': { '$gte': ?1 }, 'currentVolunteers': { '$lt': ?2 }, '$or': [ { 'status': 'ACTIVE' }, { 'status': null } ] }")
    @Update("{ '$inc': { 'currentVolunteers': 1 }, '$set': { 'updatedAt': ?3 } }")
    long incrementCurrentVolunteersIfCapacityAvailable(
            String eventId,
            Instant now,
            int maxVolunteers,
            Instant updatedAt);

    @Query("{ '_id': ?0, 'currentVolunteers': { '$gt': 0 } }")
    @Update("{ '$inc': { 'currentVolunteers': -1 }, '$set': { 'updatedAt': ?1 } }")
    long decrementCurrentVolunteersIfPositive(String eventId, Instant updatedAt);
}
