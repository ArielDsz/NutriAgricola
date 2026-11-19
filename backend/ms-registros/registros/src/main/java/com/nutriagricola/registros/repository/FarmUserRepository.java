package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FarmUserRepository extends JpaRepository<FarmUser, String> {
    boolean existsByFarm_IdAndUserId(String farmId, String userId);

    @Query("""
            select farm from FarmUser assignment
            join assignment.farm farm
            where assignment.userId = :userId
            order by farm.name, farm.id
            """)
    List<Farm> findFarmsByUserId(@Param("userId") String userId);
}
