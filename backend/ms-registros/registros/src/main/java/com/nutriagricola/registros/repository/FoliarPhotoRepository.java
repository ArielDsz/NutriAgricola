package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.FoliarPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FoliarPhotoRepository extends JpaRepository<FoliarPhoto, String> {
    Optional<FoliarPhoto> findByIdAndRecord_Id(String id, String recordId);
    List<FoliarPhoto> findByRecord_IdOrderByIdAsc(String recordId);
}
