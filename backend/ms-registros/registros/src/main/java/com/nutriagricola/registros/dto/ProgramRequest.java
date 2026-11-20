package com.nutriagricola.registros.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
public record ProgramRequest(@NotBlank @UUID String id, @NotBlank @UUID String cropId,
                              @NotNull @Valid ProgramContent content) { }
