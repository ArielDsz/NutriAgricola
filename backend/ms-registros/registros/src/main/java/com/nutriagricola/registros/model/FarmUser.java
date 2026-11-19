package com.nutriagricola.registros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "farm_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_farm_users_farm_user", columnNames = {"farm_id", "user_id"})
})
@Getter @Setter @NoArgsConstructor
public class FarmUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Farm farm;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
}
