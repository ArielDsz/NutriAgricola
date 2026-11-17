package com.nutriagricola.registros.service;

import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreateResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.dto.ReviewCreateResponse;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FoliarData;
import com.nutriagricola.registros.model.FoliarPhoto;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import com.nutriagricola.registros.model.SoilData;
import com.nutriagricola.registros.model.WaterData;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FoliarDataRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import com.nutriagricola.registros.repository.SoilDataRepository;
import com.nutriagricola.registros.repository.WaterDataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class RecordsService {
    private final FarmRepository farms;
    private final CropRepository crops;
    private final RecordRepository records;
    private final SoilDataRepository soilData;
    private final WaterDataRepository waterData;
    private final FoliarDataRepository foliarData;
    private final FoliarPhotoRepository foliarPhotos;
    private final RecordReviewRepository reviews;

    public RecordsService(FarmRepository farms, CropRepository crops, RecordRepository records,
                          SoilDataRepository soilData, WaterDataRepository waterData,
                          FoliarDataRepository foliarData, FoliarPhotoRepository foliarPhotos,
                          RecordReviewRepository reviews) {
        this.farms = farms;
        this.crops = crops;
        this.records = records;
        this.soilData = soilData;
        this.waterData = waterData;
        this.foliarData = foliarData;
        this.foliarPhotos = foliarPhotos;
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public List<FarmResponse> getFarms() {
        return farms.findAll().stream()
                .map(farm -> new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(),
                        farm.getImagePath(), farm.getActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByFarm(String farmId) {
        return crops.findByFarm_Id(farmId).stream()
                .map(crop -> new CropResponse(crop.getId(), crop.getFarm().getId(), crop.getName(),
                        crop.getVariety(), crop.getAreaHectares(), crop.getPhenologicalStage(),
                        crop.getImagePath(), crop.getActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecordSummaryResponse> getRecordsByStatus(String status) {
        return records.findByStatus(status.toUpperCase(Locale.ROOT)).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public RecordCreateResponse createRecord(RecordCreateRequest request) {
        String type = request.type().toUpperCase(Locale.ROOT);
        validateRecordData(type, request);

        Crop crop = crops.findById(request.cropId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cultivo no encontrado"));

        Record record = new Record();
        record.setId(request.id());
        record.setCrop(crop);
        record.setUserId(request.userId());
        record.setDate(request.date());
        record.setType(type);
        record.setStatus(request.status().toUpperCase(Locale.ROOT));
        record.setObservations(request.observations());
        records.save(record);

        switch (type) {
            case "SOIL" -> saveSoilData(record, request.soilData());
            case "WATER" -> saveWaterData(record, request.waterData());
            case "FOLIAR" -> saveFoliarData(record, request.foliarData(), request.foliarPhotos());
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "type debe ser SOIL, WATER o FOLIAR");
        }

        return new RecordCreateResponse(record.getId(), record.getStatus());
    }

    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        String result = request.result().toUpperCase(Locale.ROOT);
        if (!List.of("VIEWED", "VALIDATED").contains(result)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "result debe ser VIEWED o VALIDATED");
        }

        Record record = records.findById(request.recordId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Análisis no encontrado"));

        RecordReview review = new RecordReview();
        review.setId(request.id());
        review.setRecord(record);
        review.setAdvisorId(request.advisorId());
        review.setAdvisorName(request.advisorName());
        review.setDate(request.date());
        review.setResult(result);
        review.setComment(request.comment());
        reviews.save(review);

        record.setStatus(result);
        records.save(record);
        return new ReviewCreateResponse(review.getId(), record.getId(), review.getAdvisorId(),
                review.getAdvisorName(), review.getDate(), review.getResult(), review.getComment(),
                record.getStatus());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envía únicamente el bloque de datos que corresponde al tipo de análisis");
        }
    }

    private void saveSoilData(Record record, RecordCreateRequest.SoilDataRequest request) {
        SoilData data = new SoilData();
        data.setRecord(record);
        data.setPh(request.ph());
        data.setElectricalConductivity(request.electricalConductivity());
        data.setNitrogen(request.nitrogen());
        data.setPhosphorus(request.phosphorus());
        data.setPotassium(request.potassium());
        data.setCalcium(request.calcium());
        data.setMagnesium(request.magnesium());
        data.setOrganicMatter(request.organicMatter());
        soilData.save(data);
    }

    private void saveWaterData(Record record, RecordCreateRequest.WaterDataRequest request) {
        WaterData data = new WaterData();
        data.setRecord(record);
        data.setPh(request.ph());
        data.setElectricalConductivity(request.electricalConductivity());
        data.setNitrogen(request.nitrogen());
        data.setPhosphorus(request.phosphorus());
        data.setPotassium(request.potassium());
        data.setCalcium(request.calcium());
        data.setMagnesium(request.magnesium());
        data.setWaterSource(request.waterSource());
        waterData.save(data);
    }

    private void saveFoliarData(Record record, RecordCreateRequest.FoliarDataRequest request,
                                List<RecordCreateRequest.FoliarPhotoRequest> photos) {
        FoliarData data = new FoliarData();
        data.setRecord(record);
        data.setNitrogen(request.nitrogen());
        data.setPhosphorus(request.phosphorus());
        data.setPotassium(request.potassium());
        data.setCalcium(request.calcium());
        data.setMagnesium(request.magnesium());
        data.setSymptoms(request.symptoms());
        foliarData.save(data);

        if (photos != null) {
            for (RecordCreateRequest.FoliarPhotoRequest photoRequest : photos) {
                FoliarPhoto photo = new FoliarPhoto();
                photo.setId(photoRequest.id());
                photo.setRecord(record);
                photo.setPath(photoRequest.path());
                foliarPhotos.save(photo);
            }
        }
    }

    private RecordSummaryResponse toSummary(Record record) {
        return new RecordSummaryResponse(record.getId(), record.getCrop().getId(), record.getUserId(),
                record.getDate(), record.getType(), record.getStatus(), record.getObservations());
    }
}
