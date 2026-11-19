package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.RecordReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordReviewRepository extends JpaRepository<RecordReview, String> {
    boolean existsByRecord_Id(String recordId);
    List<RecordReview> findByRecord_IdOrderByDateDescIdAsc(String recordId);
}
