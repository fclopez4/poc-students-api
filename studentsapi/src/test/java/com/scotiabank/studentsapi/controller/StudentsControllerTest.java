package com.scotiabank.studentsapi.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.scotiabank.studentsapi.config.WebConfiguration;
import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.service.StudentService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest(controllers = StudentsController.class)
@Import(WebConfiguration.class)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class StudentsControllerTest {

    @MockitoBean
    private StudentService studentService;

    @Autowired
    WebTestClient webTestClient;

    private StudentRequest studentRequest;
    private StudentResponse studentResponse;
    private StudentResponse anotherStudentResponse;

    @BeforeEach
    void setUp() {
        studentRequest = StudentRequest.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status("ACTIVE")
                .age((short) 20)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status("ACTIVE")
                .age((short) 20)
                .build();

        anotherStudentResponse = StudentResponse.builder()
                .id(2L)
                .firstName("María")
                .lastName("Gómez")
                .status("ACTIVE")
                .age((short) 22)
                .build();
    }

    @Test
    void getHealthCheck_ReturnsOk_WithHealthMessage() {
        webTestClient.get()
                .uri("/api/students/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertNotNull(body);
                    assertTrue(body.startsWith("It's alive!:"));
                    assertTrue(body.contains(String.valueOf(LocalDateTime.now().getYear())));
                });
    }

    @Test
    void getHealthCheck_ReturnsBadRequest_WhenInvalidApiVersionHeader() {
        webTestClient.get()
                .uri("/api/students/health")
                .header("API-Version", "99.0.0")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createNewStudent_ReturnsCreated_WhenValidRequest() {
        when(studentService.createStudent(any(StudentRequest.class)))
                .thenReturn(Mono.just(studentResponse));

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(studentRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void createNewStudent_ReturnsPreconditionFailed_WhenStudentAlreadyExists() {
        String expectedMessage = String.format("Ya existe un estudiante con el id %d", studentRequest.getId());
        
        when(studentService.createStudent(any(StudentRequest.class)))
                .thenReturn(Mono.error(new DuplicateKeyException(expectedMessage)));

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(studentRequest)
                .exchange()
                .expectStatus().isEqualTo(412)
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedMessage);
    }

    @Test
    void createNewStudent_ReturnsPreconditionFailed_WhenInvalidRequest() {
        StudentRequest invalidRequest = StudentRequest.builder()
                .id(-1L)
                .firstName("")
                .lastName("")
                .status("UNKNOWN")
                .age((short) 200)
                .build();

        webTestClient.post()
                .uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isEqualTo(412)
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors.length()").isEqualTo(5);
    }

    @Test
    void getStudentsByStatus_ReturnsOk_WithActiveStudents() {
        when(studentService.getStudentsByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.just(studentResponse, anotherStudentResponse));

        webTestClient.get()
                .uri("/api/students?status=ACTIVE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(StudentResponse.class)
                .hasSize(2)
                .contains(studentResponse)
                .contains(anotherStudentResponse);
    }

    @Test
    void getStudentsByStatus_ReturnsOk_WithDefaultActiveStatus() {
        when(studentService.getStudentsByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.just(studentResponse, anotherStudentResponse));

        var fluxResponse = webTestClient.get()
                .uri("/api/students")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(StudentResponse.class)                
                .getResponseBody();
        
        StepVerifier.create(fluxResponse)
                .expectNext(studentResponse, anotherStudentResponse)
                .verifyComplete();
    }

    @Test
    void getStudentsByStatus_ReturnsInternalServerError_WhenUnexpectedExceptionOccurs() {
        String expectedMessage = "Unexpected error";
        when(studentService.getStudentsByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.error(new RuntimeException(expectedMessage)));

        webTestClient.get()
                .uri("/api/students?status=ACTIVE")
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message").isEqualTo(expectedMessage);
    }

}
