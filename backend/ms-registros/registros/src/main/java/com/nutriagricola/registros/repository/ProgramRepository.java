package com.nutriagricola.registros.repository;
import com.nutriagricola.registros.model.Program;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;

public interface ProgramRepository extends JpaRepository<Program, String> {
    @Query(value = """
            select p from Program p left join fetch p.crop crop
            where (:cropId is null or crop.id = :cropId)
            """, countQuery = """
            select count(p) from Program p left join p.crop crop
            where (:cropId is null or crop.id = :cropId)
            """)
    Page<Program> findPrograms(@Param("cropId") String cropId, Pageable pageable);
}
