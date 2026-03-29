package com.scotiabank.studentsapi.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
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
    @Column("ID")
    private Long id;
    @Column("FIRST_NAME")
    private String firstName;
    @Column("LAST_NAME")
    private String lastName;
    @Column("STATUS")
    private StatusStudent status;
    @Column("AGE")
    private Short age;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }
}