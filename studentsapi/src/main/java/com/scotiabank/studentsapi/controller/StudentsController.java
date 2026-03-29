package com.scotiabank.studentsapi.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.service.StudentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/students")
public class StudentsController {
 
    private final StudentService studentService;

    public StudentsController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping(version ="1.0.0", path = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getHealthCheck() {
        return Mono.just(String.format("It's alive!: %s", LocalDateTime.now()));
    }
    
    @GetMapping(version ="1.0.0", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<StudentResponse> getStudentsByStatus(@RequestParam(name = "status", defaultValue = "ACTIVE") StatusStudent status) {
        log.info("Fetching students with status: {}", status);
        return studentService.getStudentsByStatus(status);
    }

    @PostMapping(version ="1.0.0", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)   
    public Mono<Void> createNewStudent(@Valid @RequestBody StudentRequest request) {
        log.info("Creating new student with id: {}", request.id());
        return studentService.createStudent(request).thenEmpty(Mono.empty());
    }
    
}
