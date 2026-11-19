package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.WaterData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterDataRepository extends JpaRepository<WaterData, String> { }
