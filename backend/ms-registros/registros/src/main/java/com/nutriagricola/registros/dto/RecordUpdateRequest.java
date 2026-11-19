package com.nutriagricola.registros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RecordUpdateRequest(
        @NotNull LocalDate date,
        String observations,
        @Valid RecordCreateRequest.SoilDataRequest soilData,
        @Valid RecordCreateRequest.WaterDataRequest waterData,
        @Valid RecordCreateRequest.FoliarDataRequest foliarData) { }
