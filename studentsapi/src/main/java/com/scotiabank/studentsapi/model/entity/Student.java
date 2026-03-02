package com.scotiabank.studentsapi.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.scotiabank.studentsapi.model.enums.StatusStudent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("STUDENTS")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Student implements Persistable<Long> {
    
    @Id
    private Long id;
    private String name;
    private String surname;
    private StatusStudent status;
    private int age;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}