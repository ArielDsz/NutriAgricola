package com.nutriagricola.registros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
import java.time.LocalDateTime;

public record ReviewCreateRequest(
        @NotBlank @UUID String id,
        @NotBlank @UUID String recordId,
        @NotNull LocalDateTime date,
        @NotBlank String result,
        String comment) { }
