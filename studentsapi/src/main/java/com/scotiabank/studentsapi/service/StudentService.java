package com.scotiabank.studentsapi.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.scotiabank.studentsapi.mapper.StudentMapper;
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
        Student student = StudentMapper.convertToEntity(studentRequest);

        return studentRepository.existsById(student.getId())
                .doOnNext(exists -> log.info("¿Existe estudiante con ID {}? {}", student.getId(), exists))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateKeyException(
                                String.format("Ya existe un estudiante con el id %d", student.getId())));
                    }
                    return studentRepository.save(student);
                })
                .doOnSuccess(
                        savedStudent -> log.info("Estudiante guardado exitosamente con ID: {}", savedStudent.getId()))
                .map(StudentMapper::convertToDTO);
    }

    public Flux<StudentResponse> getStudentsByStatus(StatusStudent status) {
        return studentRepository.findByStatus(status)
                .map(StudentMapper::convertToDTO);
    }

}
