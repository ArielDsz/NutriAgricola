package com.nutriagricola.registros.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program_fertilizers")
@Getter @Setter @NoArgsConstructor
public class ProgramFertilizer {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "dose_kg_ha", nullable = false)
    private Double doseKgHa;
    @Column(columnDefinition = "TEXT")
    private String notes;
}
