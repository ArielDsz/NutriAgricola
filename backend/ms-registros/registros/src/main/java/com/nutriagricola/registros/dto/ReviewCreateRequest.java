package com.nutriagricola.registros.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewCreateRequest(
        @NotBlank String id,
        @NotBlank String recordId,
        @NotBlank String advisorId,
        @NotBlank String advisorName,
        @NotBlank String date,
        @NotBlank String result,
        @NotBlank String comment) { }
