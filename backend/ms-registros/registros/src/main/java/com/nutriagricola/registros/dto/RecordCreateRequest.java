package com.nutriagricola.registros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;
import java.time.LocalDate;
import java.util.List;

public record RecordCreateRequest(
        @NotBlank @UUID String id,
        @NotBlank @UUID String cropId,
        @NotNull LocalDate date,
        @NotBlank String type,
        String observations,
        @Valid SoilDataRequest soilData,
        @Valid WaterDataRequest waterData,
        @Valid FoliarDataRequest foliarData,
        List<@NotNull @Valid FoliarPhotoRequest> foliarPhotos) {

    public record SoilDataRequest(Double ph, Double electricalConductivity, Double nitrogen,
                                  Double phosphorus, Double potassium, Double calcium,
                                  Double magnesium, Double organicMatter) { }

    public record WaterDataRequest(Double ph, Double electricalConductivity, Double nitrogen,
                                   Double phosphorus, Double potassium, Double calcium,
                                   Double magnesium, @Size(max = 150) String waterSource) { }

    public record FoliarDataRequest(Double nitrogen, Double phosphorus, Double potassium,
                                    Double calcium, Double magnesium, String symptoms) { }

    public record FoliarPhotoRequest(@NotBlank @UUID String id, @NotBlank String path) { }
}
