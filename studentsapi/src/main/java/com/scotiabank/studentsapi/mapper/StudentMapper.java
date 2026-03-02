package com.scotiabank.studentsapi.mapper;

import com.scotiabank.studentsapi.model.dto.StudentRequest;
import com.scotiabank.studentsapi.model.dto.StudentResponse;
import com.scotiabank.studentsapi.model.entity.Student;
import com.scotiabank.studentsapi.model.enums.StatusStudent;

public class StudentMapper {

    public static StudentResponse convertToDTO(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .surname(student.getSurname())
                .status(student.getStatus().toString())
                .age(student.getAge())
                .build();
    }

    public static Student convertToEntity(StudentRequest dto) {
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
