package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecordRepository extends JpaRepository<Record, String> {
    @Query(value = """
            select record from Record record
            join fetch record.crop crop
            where (:status is null or record.status = :status)
            and (:type is null or record.type = :type)
            and (:cropId is null or crop.id = :cropId)
            and exists (
                select assignment.id from FarmUser assignment
                where assignment.farm = crop.farm and assignment.userId = :userId
            )
            """, countQuery = """
            select count(record) from Record record
            join record.crop crop
            where (:status is null or record.status = :status)
            and (:type is null or record.type = :type)
            and (:cropId is null or crop.id = :cropId)
            and exists (
                select assignment.id from FarmUser assignment
                where assignment.farm = crop.farm and assignment.userId = :userId
            )
            """)
    Page<Record> findHistory(@Param("userId") String userId, @Param("cropId") String cropId,
                             @Param("type") String type, @Param("status") String status, Pageable pageable);
}
