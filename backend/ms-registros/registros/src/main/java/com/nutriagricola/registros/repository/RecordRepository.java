package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, String> {
    @Query("""
            select record from Record record
            join fetch record.crop crop
            where record.status = :status
            and exists (
                select assignment.id from FarmUser assignment
                where assignment.farm = crop.farm and assignment.userId = :userId
            )
            order by record.date desc, record.id
            """)
    List<Record> findByStatusAndAssignedUser(@Param("status") String status, @Param("userId") String userId);
}
