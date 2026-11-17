package com.nutriagricola.registros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "soil_data")
@Getter @Setter @NoArgsConstructor
public class SoilData {
    @Id
    @Column(name = "record_id", length = 36)
    private String recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Record record;

    private Double ph;
    @Column(name = "electrical_conductivity")
    private Double electricalConductivity;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private Double calcium;
    private Double magnesium;
    @Column(name = "organic_matter")
    private Double organicMatter;
}
