package com.scotiabank.studentsapi.respository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;

import reactor.core.publisher.Flux;

@Repository
public interface StudentRepository extends R2dbcRepository<Student, Long> {

    Flux<Student> findByStatus(StatusStudent status);
}
