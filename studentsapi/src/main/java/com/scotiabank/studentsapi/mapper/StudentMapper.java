package com.scotiabank.studentsapi.mapper;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StudentMapper {

    public static StudentResponse convertToDTO(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .status(student.getStatus().toString())
                .age(student.getAge())
                .build();
    }

    public static Student convertToEntity(StudentRequest dto) {
        return Student.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .status(StatusStudent.valueOf(dto.getStatus()))
                .age(dto.getAge())
                .isNew(true)
                .build();
    }

}
