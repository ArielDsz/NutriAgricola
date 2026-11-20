package com.nutriagricola.registros.repository;
import com.nutriagricola.registros.model.Program;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;

public interface ProgramRepository extends JpaRepository<Program, String> {
    @Query(value = """
            select p from Program p join fetch p.crop crop
            where (:cropId is null or crop.id = :cropId)
            and exists (select a.id from FarmUser a where a.farm = crop.farm and a.userId = :userId)
            """, countQuery = """
            select count(p) from Program p join p.crop crop
            where (:cropId is null or crop.id = :cropId)
            and exists (select a.id from FarmUser a where a.farm = crop.farm and a.userId = :userId)
            """)
    Page<Program> findAssigned(@Param("userId") String userId, @Param("cropId") String cropId, Pageable pageable);
}
