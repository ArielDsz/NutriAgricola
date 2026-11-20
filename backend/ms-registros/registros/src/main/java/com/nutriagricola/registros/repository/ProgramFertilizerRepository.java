package com.nutriagricola.registros.repository;
import com.nutriagricola.registros.model.ProgramFertilizer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProgramFertilizerRepository extends JpaRepository<ProgramFertilizer, String> {
    List<ProgramFertilizer> findByProgram_IdOrderByItemOrderAsc(String programId);
    @Modifying(flushAutomatically = true)
    @Query("delete from ProgramFertilizer item where item.program.id = :programId")
    void deleteItems(@Param("programId") String programId);
}
