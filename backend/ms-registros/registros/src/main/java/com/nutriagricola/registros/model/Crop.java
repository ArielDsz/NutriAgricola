package com.nutriagricola.registros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "crops")
@Getter @Setter @NoArgsConstructor
public class Crop {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Farm farm;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String variety;

    @Column(name = "area_hectares")
    private Double areaHectares;

    @Column(name = "phenological_stage", length = 100)
    private String phenologicalStage;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(nullable = false)
    private Boolean active = true;
}
