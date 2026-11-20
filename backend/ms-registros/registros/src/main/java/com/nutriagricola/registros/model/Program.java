package com.nutriagricola.registros.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "programs")
@Getter @Setter @NoArgsConstructor
public class Program {
    @Id @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(name = "advisor_id", nullable = false, length = 36)
    private String advisorId;
    @Column(name = "advisor_name", nullable = false, length = 511)
    private String advisorName;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String season;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "target_yield_ton_ha")
    private Double targetYieldTonHa;

    @Column(name = "irrigation_efficiency_percent")
    private Double irrigationEfficiencyPercent;

    @Column(name = "nitrogen")
    private Double nitrogen;

    @Column(name = "phosphorus_oxide")
    private Double phosphorusOxide;

    @Column(name = "potassium_oxide")
    private Double potassiumOxide;

    @Column(name = "calcium_oxide")
    private Double calciumOxide;

    @Column(name = "magnesium_oxide")
    private Double magnesiumOxide;

    @Column(name = "sulfur")
    private Double sulfur;

    @Column(name = "irrigation_notes", columnDefinition = "TEXT")
    private String irrigationNotes;

    @Column(nullable = false)
    private Boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
