package com.nutriagricola.registros.repository;

import com.nutriagricola.registros.model.FoliarPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoliarPhotoRepository extends JpaRepository<FoliarPhoto, String> {
    List<FoliarPhoto> findByRecord_IdOrderByIdAsc(String recordId);
}
