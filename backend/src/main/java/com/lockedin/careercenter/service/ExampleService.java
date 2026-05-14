package com.lockedin.careercenter.service;

import java.time.Instant;
import java.util.List;

import com.lockedin.careercenter.dto.ExampleCreateRequest;
import com.lockedin.careercenter.dto.ExampleResponse;
import com.lockedin.careercenter.model.ExampleDocument;
import com.lockedin.careercenter.repository.ExampleRepository;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    private final ExampleRepository exampleRepository;

    public ExampleService(ExampleRepository exampleRepository) {
        this.exampleRepository = exampleRepository;
    }

    public List<ExampleResponse> getExamples() {
        return exampleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExampleResponse createExample(ExampleCreateRequest request) {
        ExampleDocument example = new ExampleDocument(
                request.title().trim(),
                request.message().trim(),
                Instant.now());

        return toResponse(exampleRepository.save(example));
    }

    private ExampleResponse toResponse(ExampleDocument example) {
        return new ExampleResponse(
                example.getId(),
                example.getTitle(),
                example.getMessage(),
                example.getCreatedAt());
    }
}
