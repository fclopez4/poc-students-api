package com.scotiabank.studentsapi.model.dto;

import lombok.Builder;

@Builder
public record StudentResponse(
    Long id,
    String firstName,
    String lastName,
    String status,
    Short age) {
}
