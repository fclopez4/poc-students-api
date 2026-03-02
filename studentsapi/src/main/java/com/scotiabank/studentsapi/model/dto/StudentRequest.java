package com.scotiabank.studentsapi.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest {

    @NotNull(message = "El id es obligatorio")
    @Min(value = 1, message = "El id debe ser mayor a 0")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String surname;

    @NotNull(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "El estado debe ser ACTIVE o INACTIVE")
    private String status;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 5, message = "La edad debe ser mayor a 4 años")
    @Max(value = 99, message = "La edad debe ser menor a 100 años")
    private Short age;
}
