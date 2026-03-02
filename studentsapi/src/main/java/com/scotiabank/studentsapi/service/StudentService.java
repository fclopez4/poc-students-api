package com.scotiabank.studentsapi.service;

import org.springframework.stereotype.Service;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;
import com.scotiabank.studentsapi.respository.StudentRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Mono<StudentResponse> createStudent(StudentRequest studentRequest) {
        Student student = convertToEntity(studentRequest);
        log.info("Intentando crear estudiante con ID: {}", student.getId());

        return studentRepository.existsById(student.getId())
                .doOnNext(exists -> log.info("¿Existe estudiante con ID {}? {}", student.getId(), exists))
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Estudiante con ID {} ya existe", student.getId());
                        return Mono.error(
                                new IllegalArgumentException(
                                        String.format("Ya existe un estudiante con el id %d", student.getId())));
                    } else {
                        log.info("Estudiante con ID {} no existe, procediendo a guardar", student.getId());
                        return Mono.just(student);
                    }
                })
                .flatMap(studentRepository::save)
                .doOnSuccess(savedStudent -> {
                    log.info("Estudiante guardado exitosamente con ID: {}", savedStudent.getId());
                })
                .map(this::convertToDTO);
    }

    public Flux<StudentResponse> getStudentsByStatus(StatusStudent status) {
        return studentRepository.findByStatus(status)
                .map(this::convertToDTO);
    }

    private StudentResponse convertToDTO(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .surname(student.getSurname())
                .status(student.getStatus().toString())
                .age(student.getAge())
                .build();
    }

    private Student convertToEntity(StudentRequest dto) {
        return Student.builder()
                .id(dto.getId())
                .name(dto.getName())
                .surname(dto.getSurname())
                .status(StatusStudent.valueOf(dto.getStatus()))
                .age(dto.getAge())
                .isNew(true)
                .build();
    }

}
