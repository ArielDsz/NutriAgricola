package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.PhotoResponse;
import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import com.nutriagricola.registros.mapper.RecordMapper;
import com.nutriagricola.registros.model.FoliarPhoto;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.storage.LocalPhotoStorage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoService {
    private final FoliarPhotoRepository photos;
    private final FarmUserRepository assignments;
    private final LocalPhotoStorage storage;
    private final RecordMapper mapper;

    public PhotoService(FoliarPhotoRepository photos, FarmUserRepository assignments,
                         LocalPhotoStorage storage, RecordMapper mapper) {
        this.photos = photos;
        this.assignments = assignments;
        this.storage = storage;
        this.mapper = mapper;
    }

    @Transactional
    public PhotoResponse upload(String recordId, String photoId, MultipartFile file, UserClientResponse user) {
        FoliarPhoto photo = findAssignedPhoto(recordId, photoId, user);
        if (!"FARMER".equals(user.role()) || !user.id().equals(photo.getRecord().getUserId())) {
            throw new AccessDeniedException("Solo el autor puede subir las fotografías del análisis");
        }
        if ("DELETED".equals(photo.getRecord().getStatus()) && photo.getStorageKey() == null) {
            throw new ResourceConflictException("No puedes añadir archivos a un análisis anulado");
        }
        var saved = storage.store(photoId, file);
        photo.setStorageKey(saved.storageKey());
        photo.setContentType(saved.contentType());
        photo.setSizeBytes(saved.sizeBytes());
        return mapper.toPhotoResponse(photos.save(photo));
    }

    @Transactional(readOnly = true)
    public PhotoDownload download(String recordId, String photoId, UserClientResponse user) {
        FoliarPhoto photo = findAssignedPhoto(recordId, photoId, user);
        if (photo.getStorageKey() == null) {
            throw new ResourceNotFoundException("El archivo de esta fotografía todavía no se ha subido");
        }
        return new PhotoDownload(storage.read(photo.getStorageKey()), photo.getContentType());
    }

    private FoliarPhoto findAssignedPhoto(String recordId, String photoId, UserClientResponse user) {
        FoliarPhoto photo = photos.findByIdAndRecord_Id(photoId, recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Fotografía no encontrada en este análisis"));
        String farmId = photo.getRecord().getCrop().getFarm().getId();
        if (!assignments.existsByFarm_IdAndUserId(farmId, user.id())) {
            throw new AccessDeniedException("No estás asignado a este predio");
        }
        return photo;
    }

    public record PhotoDownload(byte[] content, String contentType) { }
}
