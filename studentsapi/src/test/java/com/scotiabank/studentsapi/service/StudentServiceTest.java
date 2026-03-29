package com.scotiabank.studentsapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.respository.StudentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student activeStudent1, activeStudent2;
    private StudentRequest activeStudentRequest1;
    private StudentResponse activeStudentexpectedResponse1;

    private Student inactiveStudent1, inactiveStudent2;
    private StudentRequest inactiveRequest1;
    
    @BeforeEach
    void setUp() {
        activeStudent1 = Student.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status(StatusStudent.ACTIVE)
                .age((short) 20)
                .isNew(true)
                .build();

        activeStudent2 = Student.builder()
                .id(2L)
                .firstName("María")
                .lastName("González")
                .status(StatusStudent.ACTIVE)
                .age((short) 22)
                .build();

        activeStudentRequest1 = StudentRequest.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status("ACTIVE")
                .age((short) 20)
                .build();

        activeStudentexpectedResponse1 = StudentResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status("ACTIVE")
                .age((short) 20)
                .build();

        inactiveStudent1 = Student.builder()
                .id(2L)
                .firstName("Alicia")
                .lastName("Gómez")
                .status(StatusStudent.INACTIVE)
                .age((short) 22)
                .isNew(true)
                .build();

        inactiveStudent2 = Student.builder()
                .id(3L)
                .firstName("Pedro")
                .lastName("Ramírez")
                .status(StatusStudent.INACTIVE)
                .age((short) 25)
                .build();

        inactiveRequest1 = StudentRequest.builder()
                .id(2L)
                .firstName("Alicia")
                .lastName("Gómez")
                .status("INACTIVE")
                .age((short) 22)
                .build();
    }

    @Test
    void createStudent_Success_WhenStudentDoesNotExist() {
        // Arrange
        when(studentRepository.existsById(anyLong())).thenReturn(Mono.just(false));
        when(studentRepository.save(any(Student.class))).thenReturn(Mono.just(activeStudent1));

        // Act
        Mono<StudentResponse> result = studentService.createStudent(activeStudentRequest1);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(activeStudentexpectedResponse1.id(), response.id());
                    assertEquals(activeStudentexpectedResponse1.firstName(), response.firstName());
                    assertEquals(activeStudentexpectedResponse1.lastName(), response.lastName());
                    assertEquals(activeStudentexpectedResponse1.status(), response.status());
                    assertEquals(activeStudentexpectedResponse1.age(), response.age());
                })
                .verifyComplete();

        verify(studentRepository, times(1)).existsById(1L);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createStudent_ThrowsDuplicateKeyException_WhenStudentExists() {
        // Arrange
        when(studentRepository.existsById(anyLong())).thenReturn(Mono.just(true));

        // Act
        Mono<StudentResponse> result = studentService.createStudent(activeStudentRequest1);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof DuplicateKeyException &&
                        throwable.getMessage().contains("Student with id 1 already exists"))
                .verify();
    }

    @Test
    void createStudent_HandlesInactiveStatus_Successfully() {
        // Arrange
        when(studentRepository.existsById(inactiveStudent1.getId())).thenReturn(Mono.just(false));
        when(studentRepository.save(any(Student.class))).thenReturn(Mono.just(inactiveStudent1));

        // Act
        Mono<StudentResponse> result = studentService.createStudent(inactiveRequest1);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(inactiveStudent1.getId(), response.id());
                    assertEquals(inactiveStudent1.getFirstName(), response.firstName());
                    assertEquals(inactiveStudent1.getLastName(), response.lastName());
                    assertEquals(inactiveStudent1.getStatus().toString(), response.status());
                    assertEquals(inactiveStudent1.getAge(), response.age());
                })
                .verifyComplete();

        verify(studentRepository, times(1)).existsById(inactiveStudent1.getId());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void getStudentsByStatus_ReturnsActiveStudents_Successfully() {
        // Arrange
        when(studentRepository.findByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.just(activeStudent1, activeStudent2));

        // Act
        Flux<StudentResponse> result = studentService.getStudentsByStatus(StatusStudent.ACTIVE);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(activeStudent1.getId(), response.id());
                    assertEquals(activeStudent1.getFirstName(), response.firstName());
                    assertEquals(activeStudent1.getStatus().toString(), response.status());
                })
                .assertNext(response -> {
                    assertEquals(activeStudent2.getId(), response.id());
                    assertEquals(activeStudent2.getFirstName(), response.firstName());
                    assertEquals(activeStudent2.getStatus().toString(), response.status());
                })
                .verifyComplete();

        verify(studentRepository, times(1)).findByStatus(StatusStudent.ACTIVE);
    }

    @Test
    void getStudentsByStatus_ReturnsInactiveStudents_Successfully() {
        // Arrange
        when(studentRepository.findByStatus(StatusStudent.INACTIVE))
                .thenReturn(Flux.just(inactiveStudent1, inactiveStudent2));

        // Act
        Flux<StudentResponse> result = studentService.getStudentsByStatus(StatusStudent.INACTIVE);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(inactiveStudent1.getId(), response.id());
                    assertEquals(inactiveStudent1.getFirstName(), response.firstName());
                    assertEquals(inactiveStudent1.getStatus().toString(), response.status());
                })
                .assertNext(response -> {
                    assertEquals(inactiveStudent2.getId(), response.id());
                    assertEquals(inactiveStudent2.getFirstName(), response.firstName());
                    assertEquals(inactiveStudent2.getStatus().toString(), response.status());
                })
                .verifyComplete();

        verify(studentRepository, times(1)).findByStatus(StatusStudent.INACTIVE);
    }

    @Test
    void getStudentsByStatus_ReturnsEmpty_WhenNoStudentsFound() {
        // Arrange
        when(studentRepository.findByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.empty());

        // Act
        Flux<StudentResponse> result = studentService.getStudentsByStatus(StatusStudent.ACTIVE);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(studentRepository, times(1)).findByStatus(StatusStudent.ACTIVE);
    }

    @Test
    void getStudentsByStatus_PropagatesError_WhenRepositoryFails() {
        // Arrange
        when(studentRepository.findByStatus(StatusStudent.ACTIVE))
                .thenReturn(Flux.error(new RuntimeException("Database connection error")));

        // Act
        Flux<StudentResponse> result = studentService.getStudentsByStatus(StatusStudent.ACTIVE);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database connection error"))
                .verify();

        verify(studentRepository, times(1)).findByStatus(StatusStudent.ACTIVE);
    }

}
