package com.lockedin.careercenter.repository;

import java.util.List;

import com.lockedin.careercenter.model.ExampleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExampleRepository extends MongoRepository<ExampleDocument, String> {

    List<ExampleDocument> findAllByOrderByCreatedAtDesc();
}
