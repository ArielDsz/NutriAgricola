package com.nutriagricola.registros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

public record CropCreateRequest(
        @NotBlank @UUID String id,
        @NotBlank @UUID String farmId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String variety,
        @Positive Double areaHectares,
        @Size(max = 100) String phenologicalStage,
        @Size(max = 255) String imagePath) { }
