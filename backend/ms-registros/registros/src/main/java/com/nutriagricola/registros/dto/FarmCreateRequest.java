package com.nutriagricola.registros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

public record FarmCreateRequest(
        @NotBlank @UUID String id,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 150) String location,
        @Size(max = 255) String imagePath) { }
