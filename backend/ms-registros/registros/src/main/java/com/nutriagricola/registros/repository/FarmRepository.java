package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmRepository extends JpaRepository<Farm, String> { }
