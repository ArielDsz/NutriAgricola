package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.SoilData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoilDataRepository extends JpaRepository<SoilData, String> { }
