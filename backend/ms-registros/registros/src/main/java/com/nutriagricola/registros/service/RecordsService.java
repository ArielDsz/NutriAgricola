package com.nutriagricola.registros.service;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreationResult;
import com.nutriagricola.registros.dto.RecordPageResponse;
import com.nutriagricola.registros.dto.RecordDetailResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.dto.RecordUpdateRequest;
import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.dto.ReviewCreateResponse;
import com.nutriagricola.registros.dto.ReviewCreationResult;
import com.nutriagricola.registros.exception.InvalidRecordException;
import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import com.nutriagricola.registros.mapper.RecordMapper;
import com.nutriagricola.registros.mapper.RecordReviewMapper;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import com.nutriagricola.registros.model.SoilData;
import com.nutriagricola.registros.model.WaterData;
import com.nutriagricola.registros.model.FoliarData;
import com.nutriagricola.registros.model.FoliarPhoto;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarDataRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import com.nutriagricola.registros.repository.SoilDataRepository;
import com.nutriagricola.registros.repository.WaterDataRepository;
import com.nutriagricola.registros.validation.RecordValidator;
import com.nutriagricola.registros.validation.RecordRequestMatcher;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private final RecordMapper recordMapper;
    private final RecordReviewMapper recordReviewMapper;
    private final RecordValidator recordValidator;
    private final RecordRequestMatcher recordRequestMatcher;
    private final EntityManager entityManager;

    public RecordsService(FarmUserRepository farmUsers, CropRepository crops, RecordRepository records,
                          SoilDataRepository soilData, WaterDataRepository waterData,
                          FoliarDataRepository foliarData, FoliarPhotoRepository foliarPhotos,
                          RecordReviewRepository reviews,
                          RecordMapper recordMapper, RecordReviewMapper recordReviewMapper,
                          RecordValidator recordValidator, RecordRequestMatcher recordRequestMatcher,
                          EntityManager entityManager) {
        this.farmUsers = farmUsers;
        this.crops = crops;
        this.records = records;
        this.soilData = soilData;
        this.waterData = waterData;
        this.foliarData = foliarData;
        this.foliarPhotos = foliarPhotos;
        this.reviews = reviews;
        this.recordMapper = recordMapper;
        this.recordReviewMapper = recordReviewMapper;
        this.recordValidator = recordValidator;
        this.recordRequestMatcher = recordRequestMatcher;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public RecordPageResponse getHistory(String cropId, String type, String status, int page, int size,
                                          UserClientResponse user) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRecordException("page debe ser mayor o igual a 0 y size debe estar entre 1 y 100");
        }
        String normalizedType = normalizeFilter(type, List.of("SOIL", "WATER", "FOLIAR"), "type");
        String normalizedStatus = normalizeFilter(status,
                List.of("DRAFT", "PENDING", "VIEWED", "VALIDATED", "DELETED"), "status");
        if (cropId != null) {
            Crop crop = crops.findById(cropId).orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado"));
            requireFarmAccess(crop.getFarm().getId(), user);
        }
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id")));
        var result = records.findHistory(user.id(), cropId, normalizedType, normalizedStatus, pageable);
        return new RecordPageResponse(result.getContent().stream().map(recordMapper::toSummaryResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RecordDetailResponse getDetail(String id, UserClientResponse user) {
        Record record = records.findById(id).orElseThrow(() -> new ResourceNotFoundException("Análisis no encontrado"));
        requireFarmAccess(record.getCrop().getFarm().getId(), user);

        return loadDetail(record, true);
    }

    private RecordDetailResponse loadDetail(Record record, boolean includeReviews) {
        String id = record.getId();
        SoilData soil = null;
        WaterData water = null;
        FoliarData foliar = null;
        List<FoliarPhoto> photos = List.of();
        switch (record.getType()) {
            case "SOIL" -> soil = soilData.findById(id).orElse(null);
            case "WATER" -> water = waterData.findById(id).orElse(null);
            case "FOLIAR" -> {
                foliar = foliarData.findById(id).orElse(null);
                photos = foliarPhotos.findByRecord_IdOrderByIdAsc(id);
            }
            default -> throw new InvalidRecordException("El análisis tiene un tipo no reconocido");
        }
        return recordMapper.toDetailResponse(record, soil, water, foliar, photos,
                includeReviews ? reviews.findByRecord_IdOrderByDateDescIdAsc(id) : List.of());
    }

    private String normalizeFilter(String value, List<String> allowed, String field) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new InvalidRecordException(field + " debe ser uno de: " + String.join(", ", allowed));
        }
        return normalized;
    }

    @Transactional
    public RecordCreationResult createRecord(RecordCreateRequest request, UserClientResponse user) {
        requireRole(user, "FARMER");
        String type = request.type().strip().toUpperCase(Locale.ROOT);
        recordValidator.validate(type, request);

        Record existing = records.findById(request.id()).orElse(null);
        if (existing != null) {
            requireFarmAccess(existing.getCrop().getFarm().getId(), user);
            if (!user.id().equals(existing.getUserId())) {
                throw new AccessDeniedException("Solo el autor puede reenviar este análisis");
            }
            if (!recordRequestMatcher.matches(request, loadDetail(existing, false))) {
                throw new ResourceConflictException("El identificador ya pertenece a un análisis con datos diferentes");
            }
            return new RecordCreationResult(recordMapper.toCreateResponse(existing), false);
        }

        Crop crop = crops.findById(request.cropId()).orElseThrow(() ->
                new ResourceNotFoundException("Cultivo no encontrado"));
        requireFarmAccess(crop.getFarm().getId(), user);

        if (!Boolean.TRUE.equals(crop.getFarm().getActive())) {
            throw new ResourceConflictException("No se pueden crear análisis en un predio inactivo");
        }
        if (!Boolean.TRUE.equals(crop.getActive())) {
            throw new ResourceConflictException("No se pueden crear análisis en un cultivo inactivo");
        }
        if (request.foliarPhotos() != null && !request.foliarPhotos().isEmpty()) {
            var photoIds = request.foliarPhotos().stream().map(RecordCreateRequest.FoliarPhotoRequest::id).toList();
            if (!foliarPhotos.findAllById(photoIds).isEmpty()) {
                throw new ResourceConflictException("Ya existe una fotografía con uno de los identificadores indicados");
            }
        }

        Record record = recordMapper.toEntity(request, crop, type, "PENDING", user.id());
        entityManager.persist(record);

        switch (type) {
            case "SOIL" -> soilData.save(recordMapper.toSoilData(request.soilData(), record));
            case "WATER" -> waterData.save(recordMapper.toWaterData(request.waterData(), record));
            case "FOLIAR" -> {
                foliarData.save(recordMapper.toFoliarData(request.foliarData(), record));
                foliarPhotos.saveAll(recordMapper.toFoliarPhotos(request.foliarPhotos(), record));
            }
            default -> throw new InvalidRecordException("type debe ser SOIL, WATER o FOLIAR");
        }

        return new RecordCreationResult(recordMapper.toCreateResponse(record), true);
    }

    @Transactional
    public ReviewCreationResult createReview(ReviewCreateRequest request, UserClientResponse user) {
        requireRole(user, "ADVISOR");
        String result = request.result().strip().toUpperCase(Locale.ROOT);
        if (!List.of("OBSERVED", "VALIDATED").contains(result)) {
            throw new InvalidRecordException("result debe ser OBSERVED o VALIDATED");
        }
        if ("OBSERVED".equals(result) && (request.comment() == null || request.comment().isBlank())) {
            throw new InvalidRecordException("Las observaciones del asesor requieren un comentario");
        }

        RecordReview existing = reviews.findById(request.id()).orElse(null);
        if (existing != null) {
            requireFarmAccess(existing.getRecord().getCrop().getFarm().getId(), user);
            if (!existing.getAdvisorId().equals(user.id())) {
                throw new AccessDeniedException("Solo el asesor autor puede reenviar esta revisión");
            }
            String comment = request.comment() == null ? "" : request.comment().strip();
            if (!existing.getRecord().getId().equals(request.recordId())
                    || !Objects.equals(existing.getDate(), request.date().toString())
                    || !Objects.equals(existing.getResult(), result)
                    || !Objects.equals(existing.getComment(), comment)) {
                throw new ResourceConflictException("La revisión ya existe con datos diferentes");
            }
            return new ReviewCreationResult(recordReviewMapper.toResponse(existing, existing.getRecord().getStatus()), false);
        }

        Record record = records.findById(request.recordId()).orElseThrow(() ->
                new ResourceNotFoundException("Análisis no encontrado"));
        requireFarmAccess(record.getCrop().getFarm().getId(), user);

        if (!List.of("PENDING", "VIEWED", "VALIDATED").contains(record.getStatus())) {
            throw new ResourceConflictException("Este análisis no está disponible para revisión");
        }

        RecordReview review = recordReviewMapper.toEntity(request, record, result,
                user.id(), user.firstName() + " " + user.lastName());
        entityManager.persist(review);

        record.setStatus("OBSERVED".equals(result) ? "PENDING" : "VALIDATED");
        records.save(record);
        return new ReviewCreationResult(recordReviewMapper.toResponse(review, record.getStatus()), true);
    }

    @Transactional
    public RecordSummaryResponse updateRecord(String id, RecordUpdateRequest request, UserClientResponse user) {
        requireRole(user, "FARMER");
        Record record = findRecordForAuthor(id, user);
        if (!"PENDING".equals(record.getStatus()) || reviews.existsByRecord_Id(id)) {
            throw new ResourceConflictException("Solo puedes editar un análisis pendiente que todavía no tenga revisiones");
        }

        List<RecordCreateRequest.FoliarPhotoRequest> existingPhotos = "FOLIAR".equals(record.getType())
                ? foliarPhotos.findByRecord_IdOrderByIdAsc(id).stream()
                    .map(photo -> new RecordCreateRequest.FoliarPhotoRequest(photo.getId(), photo.getPath())).toList()
                : List.of();
        var validationRequest = new RecordCreateRequest(record.getId(), record.getCrop().getId(), request.date(),
                record.getType(), request.observations(), request.soilData(), request.waterData(),
                request.foliarData(), existingPhotos);
        recordValidator.validate(record.getType(), validationRequest);

        switch (record.getType()) {
            case "SOIL" -> {
                SoilData data = soilData.findById(id).orElseThrow(() ->
                        new ResourceConflictException("Faltan los datos del análisis de suelo"));
                recordMapper.updateSoilData(request.soilData(), data);
                soilData.save(data);
            }
            case "WATER" -> {
                WaterData data = waterData.findById(id).orElseThrow(() ->
                        new ResourceConflictException("Faltan los datos del análisis de agua"));
                recordMapper.updateWaterData(request.waterData(), data);
                waterData.save(data);
            }
            case "FOLIAR" -> {
                FoliarData data = foliarData.findById(id).orElseThrow(() ->
                        new ResourceConflictException("Faltan los datos del análisis foliar"));
                recordMapper.updateFoliarData(request.foliarData(), data);
                foliarData.save(data);
            }
        }
        record.setDate(request.date().toString());
        record.setObservations(request.observations());
        return recordMapper.toSummaryResponse(records.save(record));
    }

    @Transactional
    public void deleteRecord(String id, UserClientResponse user) {
        requireRole(user, "FARMER");
        Record record = findRecordForAuthor(id, user);
        record.setStatus("DELETED");
        records.save(record);
    }

    private Record findRecordForAuthor(String id, UserClientResponse user) {
        Record record = records.findById(id).orElseThrow(() -> new ResourceNotFoundException("Análisis no encontrado"));
        requireFarmAccess(record.getCrop().getFarm().getId(), user);
        if (!user.id().equals(record.getUserId())) {
            throw new AccessDeniedException("Solo el autor puede modificar o anular este análisis");
        }
        return record;
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

}
