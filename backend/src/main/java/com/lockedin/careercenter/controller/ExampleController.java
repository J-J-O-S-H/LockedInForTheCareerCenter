package com.lockedin.careercenter.controller;

import java.util.List;

import com.lockedin.careercenter.dto.ExampleCreateRequest;
import com.lockedin.careercenter.dto.ExampleResponse;
import com.lockedin.careercenter.service.ExampleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/example")
public class ExampleController {

    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping
    public List<ExampleResponse> getExamples() {
        return exampleService.getExamples();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExampleResponse createExample(@Valid @RequestBody ExampleCreateRequest request) {
        return exampleService.createExample(request);
    }
}
