package com.nutriagricola.registros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

// Todos los valores son ingresados manualmente; este prototipo no calcula recomendaciones.
public record ProgramContent(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 30) String season,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @PositiveOrZero Double targetYieldTonHa,
        @PositiveOrZero @DecimalMax("100") Double irrigationEfficiencyPercent,
        @NotNull @Valid Nutrients nutrients,
        String irrigationNotes,
        @NotNull @Size(max = 100) List<@NotNull @Valid Fertilizer> fertilizers,
        @NotNull @Size(max = 20) List<@NotNull @Valid Stage> stages) {
    // Demandas manuales en kg/ha, expresadas como N, P2O5, K2O, CaO, MgO y S.
    public record Nutrients(@PositiveOrZero Double nitrogen, @PositiveOrZero Double phosphorusOxide,
                             @PositiveOrZero Double potassiumOxide, @PositiveOrZero Double calciumOxide,
                             @PositiveOrZero Double magnesiumOxide, @PositiveOrZero Double sulfur) { }
    public record Fertilizer(@NotBlank @Size(max = 150) String name,
                              @NotNull @PositiveOrZero Double doseKgHa, String notes) { }
    public record Stage(@NotBlank @Size(max = 100) String name, @Positive Integer weeks,
                         @PositiveOrZero Integer irrigationsPerWeek, String instructions) { }
}
