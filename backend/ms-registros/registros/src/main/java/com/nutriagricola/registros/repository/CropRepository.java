package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropRepository extends JpaRepository<Crop, String> {
    List<Crop> findByFarm_IdOrderByNameAscIdAsc(String farmId);
}
