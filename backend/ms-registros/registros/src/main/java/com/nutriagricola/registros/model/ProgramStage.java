package com.nutriagricola.registros.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program_stages")
@Getter @Setter @NoArgsConstructor
public class ProgramStage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;
    @Column(nullable = false, length = 100)
    private String name;
    private Integer weeks;
    @Column(name = "irrigations_per_week")
    private Integer irrigationsPerWeek;
    @Column(columnDefinition = "TEXT")
    private String instructions;
    @Column(name = "allocation_percent")
    private Double allocationPercent;
}
