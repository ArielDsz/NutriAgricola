package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, String> {
    List<Record> findByStatus(String status);
}
