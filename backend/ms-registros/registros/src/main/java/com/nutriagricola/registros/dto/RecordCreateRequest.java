package com.nutriagricola.registros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RecordCreateRequest(
        @NotBlank String id,
        @NotBlank String cropId,
        @NotBlank String userId,
        @NotBlank String date,
        @NotBlank String type,
        @NotBlank String status,
        String observations,
        @Valid SoilDataRequest soilData,
        @Valid WaterDataRequest waterData,
        @Valid FoliarDataRequest foliarData,
        @Valid List<@Valid FoliarPhotoRequest> foliarPhotos) {

    public record SoilDataRequest(Double ph, Double electricalConductivity, Double nitrogen,
                                  Double phosphorus, Double potassium, Double calcium,
                                  Double magnesium, Double organicMatter) { }

    public record WaterDataRequest(Double ph, Double electricalConductivity, Double nitrogen,
                                   Double phosphorus, Double potassium, Double calcium,
                                   Double magnesium, String waterSource) { }

    public record FoliarDataRequest(Double nitrogen, Double phosphorus, Double potassium,
                                    Double calcium, Double magnesium, String symptoms) { }

    public record FoliarPhotoRequest(@NotBlank String id, @NotBlank String path) { }
}
