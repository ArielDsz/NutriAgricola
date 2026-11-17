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
@Table(name = "record_reviews")
@Getter @Setter @NoArgsConstructor
public class RecordReview {
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Record record;

    @Column(name = "advisor_id", nullable = false, length = 36)
    private String advisorId;

    @Column(name = "advisor_name", nullable = false, length = 150)
    private String advisorName;

    @Column(nullable = false, length = 30)
    private String date;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
}
