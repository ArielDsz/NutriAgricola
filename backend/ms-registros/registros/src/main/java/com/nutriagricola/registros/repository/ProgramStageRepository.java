package com.nutriagricola.registros.repository;
import com.nutriagricola.registros.model.ProgramStage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProgramStageRepository extends JpaRepository<ProgramStage, String> {
    List<ProgramStage> findByProgram_IdOrderByItemOrderAsc(String programId);
    @Modifying(flushAutomatically = true)
    @Query("delete from ProgramStage item where item.program.id = :programId")
    void deleteItems(@Param("programId") String programId);
}
