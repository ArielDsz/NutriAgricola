package com.nutriagricola.registros.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;
public record ProgramRequest(@NotBlank @UUID String id,
                              @Size(max = 150) String species, @Size(max = 150) String plot,
                              @NotNull @Valid ProgramContent content) { }
