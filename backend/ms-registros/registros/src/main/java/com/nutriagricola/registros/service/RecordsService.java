package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreateResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.dto.ReviewCreateResponse;
import com.nutriagricola.registros.exception.InvalidRecordException;
import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import com.nutriagricola.registros.mapper.CropMapper;
import com.nutriagricola.registros.mapper.RecordMapper;
import com.nutriagricola.registros.mapper.RecordReviewMapper;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarDataRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import com.nutriagricola.registros.repository.SoilDataRepository;
import com.nutriagricola.registros.repository.WaterDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class RecordsService {
    private final FarmUserRepository farmUsers;
    private final CropRepository crops;
    private final RecordRepository records;
    private final SoilDataRepository soilData;
    private final WaterDataRepository waterData;
    private final FoliarDataRepository foliarData;
    private final FoliarPhotoRepository foliarPhotos;
    private final RecordReviewRepository reviews;
    private final CropMapper cropMapper;
    private final RecordMapper recordMapper;
    private final RecordReviewMapper recordReviewMapper;

    public RecordsService(FarmUserRepository farmUsers, CropRepository crops, RecordRepository records,
                          SoilDataRepository soilData, WaterDataRepository waterData,
                          FoliarDataRepository foliarData, FoliarPhotoRepository foliarPhotos,
                          RecordReviewRepository reviews, CropMapper cropMapper,
                          RecordMapper recordMapper, RecordReviewMapper recordReviewMapper) {
        this.farmUsers = farmUsers;
        this.crops = crops;
        this.records = records;
        this.soilData = soilData;
        this.waterData = waterData;
        this.foliarData = foliarData;
        this.foliarPhotos = foliarPhotos;
        this.reviews = reviews;
        this.cropMapper = cropMapper;
        this.recordMapper = recordMapper;
        this.recordReviewMapper = recordReviewMapper;
    }

    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByFarm(String farmId, UserClientResponse user) {
        requireFarmAccess(farmId, user);
        return crops.findByFarm_Id(farmId).stream().map(cropMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RecordSummaryResponse> getRecordsByStatus(String status, UserClientResponse user) {
        return records.findByStatusAndAssignedUser(status.toUpperCase(Locale.ROOT), user.id()).stream()
                .map(recordMapper::toSummaryResponse)
                .toList();
    }

    @Transactional
    public RecordCreateResponse createRecord(RecordCreateRequest request, UserClientResponse user) {
        requireRole(user, "FARMER");
        String type = request.type().toUpperCase(Locale.ROOT);
        validateRecordData(type, request);

        if (records.existsById(request.id())) {
            throw new ResourceConflictException("Ya existe un análisis con el identificador indicado");
        }

        Crop crop = crops.findById(request.cropId()).orElseThrow(() ->
                new ResourceNotFoundException("Cultivo no encontrado"));
        requireFarmAccess(crop.getFarm().getId(), user);

        if (!Boolean.TRUE.equals(crop.getFarm().getActive())) {
            throw new ResourceConflictException("No se pueden crear análisis en un predio inactivo");
        }

        Record record = recordMapper.toEntity(request, crop, type, "PENDING", user.id());
        record = records.save(record);

        switch (type) {
            case "SOIL" -> soilData.save(recordMapper.toSoilData(request.soilData(), record));
            case "WATER" -> waterData.save(recordMapper.toWaterData(request.waterData(), record));
            case "FOLIAR" -> {
                foliarData.save(recordMapper.toFoliarData(request.foliarData(), record));
                foliarPhotos.saveAll(recordMapper.toFoliarPhotos(request.foliarPhotos(), record));
            }
            default -> throw new InvalidRecordException("type debe ser SOIL, WATER o FOLIAR");
        }

        return recordMapper.toCreateResponse(record);
    }

    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request, UserClientResponse user) {
        requireRole(user, "ADVISOR");
        String result = request.result().toUpperCase(Locale.ROOT);
        if (!List.of("VIEWED", "VALIDATED").contains(result)) {
            throw new InvalidRecordException("result debe ser VIEWED o VALIDATED");
        }

        Record record = records.findById(request.recordId()).orElseThrow(() ->
                new ResourceNotFoundException("Análisis no encontrado"));
        requireFarmAccess(record.getCrop().getFarm().getId(), user);

        if (reviews.existsById(request.id())) {
            throw new ResourceConflictException("Ya existe una revisión con el identificador indicado");
        }

        RecordReview review = recordReviewMapper.toEntity(request, record, result,
                user.id(), user.firstName() + " " + user.lastName());
        reviews.save(review);

        record.setStatus(result);
        records.save(record);
        return recordReviewMapper.toResponse(review, record.getStatus());
    }

    private void requireFarmAccess(String farmId, UserClientResponse user) {
        if (!farmUsers.existsByFarm_IdAndUserId(farmId, user.id())) {
            throw new AccessDeniedException("No estás asignado a este predio");
        }
    }

    private void requireRole(UserClientResponse user, String role) {
        if (!role.equals(user.role())) {
            throw new AccessDeniedException("No tienes permiso para realizar esta operación");
        }
    }

    private void validateRecordData(String type, RecordCreateRequest request) {
        boolean valid = switch (type) {
            case "SOIL" -> request.soilData() != null && request.waterData() == null
                    && request.foliarData() == null && (request.foliarPhotos() == null || request.foliarPhotos().isEmpty());
            case "WATER" -> request.waterData() != null && request.soilData() == null
                    && request.foliarData() == null && (request.foliarPhotos() == null || request.foliarPhotos().isEmpty());
            case "FOLIAR" -> request.foliarData() != null && request.soilData() == null
                    && request.waterData() == null;
            default -> false;
        };
        if (!valid) {
            throw new InvalidRecordException(
                    "Envía únicamente el bloque de datos que corresponde al tipo de análisis");
        }
    }

}
