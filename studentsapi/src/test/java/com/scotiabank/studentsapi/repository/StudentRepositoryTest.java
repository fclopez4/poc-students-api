package com.scotiabank.studentsapi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;

import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.respository.StudentRepository;

import reactor.test.StepVerifier;

@DataR2dbcTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student activeStudent;
    private Student inactiveStudent;

    @BeforeEach
    void setUp() {
        activeStudent = Student.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .status(StatusStudent.ACTIVE)
                .age((short) 20)
                .isNew(true)
                .build();

        inactiveStudent = Student.builder()
                .id(2L)
                .firstName("María")
                .lastName("Gómez")
                .status(StatusStudent.INACTIVE)
                .age((short) 22)
                .isNew(true)
                .build();

        studentRepository.deleteAll()
                .then(studentRepository.save(activeStudent))
                .then(studentRepository.save(inactiveStudent))
                .block();
    }

    @Test
    void findByStatus_ReturnActiveStudents() {
        StepVerifier.create(studentRepository.findByStatus(StatusStudent.ACTIVE))
                .expectNextMatches(s -> s.getId().equals(1L)
                        && s.getFirstName().equals("Juan")
                        && s.getStatus() == StatusStudent.ACTIVE)
                .verifyComplete();
    }

    @Test
    void findByStatus_ReturnInactiveStudents() {
        StepVerifier.create(studentRepository.findByStatus(StatusStudent.INACTIVE))
                .expectNextMatches(s -> s.getId().equals(2L)
                        && s.getFirstName().equals("María")
                        && s.getStatus() == StatusStudent.INACTIVE)
                .verifyComplete();
    }

    @Test
    void findByStatus_ReturnEmpty_WhenNoStudentsMatchStatus() {
        studentRepository.deleteAll().block();

        StepVerifier.create(studentRepository.findByStatus(StatusStudent.ACTIVE))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void save_PersistStudentCorrectly() {
        Student newStudent = Student.builder()
                .id(3L)
                .firstName("Carlos")
                .lastName("López")
                .status(StatusStudent.ACTIVE)
                .age((short) 25)
                .isNew(true)
                .build();

        StepVerifier.create(studentRepository.save(newStudent))
                .expectNext(newStudent)
                .verifyComplete();
    }

    @Test
    void existsById_ReturnTrue_WhenStudentExists() {
        StepVerifier.create(studentRepository.existsById(1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_ReturnFalse_WhenStudentDoesNotExist() {
        StepVerifier.create(studentRepository.existsById(99L))
                .expectNext(false)
                .verifyComplete();
    }
}
