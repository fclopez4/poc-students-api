package com.scotiabank.studentsapi.controller;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.service.StudentService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/students")
public class StudentsController {
 
    private final StudentService studentService;

    public StudentsController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<StudentResponse> getStudentsByStatus(@RequestParam(defaultValue = "ACTIVE") StatusStudent status) {
        return studentService.getStudentsByStatus(status);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<StudentResponse> createNewStudent(@Valid @RequestBody StudentRequest request) {
        return studentService.createStudent(request);
    }
    

    /*@GetMapping(produces = MediaType.TEXT_PLAIN_VALUE, version = "1")
    public Mono<String> getStudents(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "") String cursor) {
        
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (cursor == null) {
            cursor = "";
        }
        
        return Mono.just("List of students with limit: " + limit + ", cursor: " + cursor);
    }*/
    
}
