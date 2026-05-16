package com.lockedin.careercenter.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.lockedin.careercenter.model.UserDocument;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
