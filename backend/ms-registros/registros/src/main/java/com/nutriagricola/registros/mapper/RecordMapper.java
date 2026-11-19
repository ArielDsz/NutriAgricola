package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordCreateResponse;
import com.nutriagricola.registros.dto.RecordSummaryResponse;
import com.nutriagricola.registros.dto.RecordDetailResponse;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.FoliarData;
import com.nutriagricola.registros.model.FoliarPhoto;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import com.nutriagricola.registros.model.SoilData;
import com.nutriagricola.registros.model.WaterData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecordMapper {
    public RecordDetailResponse toDetailResponse(Record record, SoilData soil, WaterData water,
                                                 FoliarData foliar, List<FoliarPhoto> photos,
                                                 List<RecordReview> reviews) {
        var soilResponse = soil == null ? null : new RecordDetailResponse.SoilDataResponse(
                soil.getPh(), soil.getElectricalConductivity(), soil.getNitrogen(), soil.getPhosphorus(),
                soil.getPotassium(), soil.getCalcium(), soil.getMagnesium(), soil.getOrganicMatter());
        var waterResponse = water == null ? null : new RecordDetailResponse.WaterDataResponse(
                water.getPh(), water.getElectricalConductivity(), water.getNitrogen(), water.getPhosphorus(),
                water.getPotassium(), water.getCalcium(), water.getMagnesium(), water.getWaterSource());
        var foliarResponse = foliar == null ? null : new RecordDetailResponse.FoliarDataResponse(
                foliar.getNitrogen(), foliar.getPhosphorus(), foliar.getPotassium(),
                foliar.getCalcium(), foliar.getMagnesium(), foliar.getSymptoms());
        var photoResponses = photos.stream()
                .map(photo -> new RecordDetailResponse.FoliarPhotoResponse(photo.getId(), photo.getPath()))
                .toList();
        var reviewResponses = reviews.stream()
                .map(review -> new RecordDetailResponse.ReviewResponse(review.getId(), review.getAdvisorId(),
                        review.getAdvisorName(), review.getDate(), review.getResult(), review.getComment()))
                .toList();
        return new RecordDetailResponse(record.getId(), record.getCrop().getId(), record.getUserId(),
                record.getDate(), record.getType(), record.getStatus(), record.getObservations(),
                soilResponse, waterResponse, foliarResponse, photoResponses, reviewResponses);
    }

    public RecordSummaryResponse toSummaryResponse(Record record) {
        return new RecordSummaryResponse(record.getId(), record.getCrop().getId(), record.getUserId(),
                record.getDate(), record.getType(), record.getStatus(), record.getObservations());
    }

    public Record toEntity(RecordCreateRequest request, Crop crop, String type, String status, String userId) {
        Record record = new Record();
        record.setId(request.id());
        record.setCrop(crop);
        record.setUserId(userId);
        record.setDate(request.date());
        record.setType(type);
        record.setStatus(status);
        record.setObservations(request.observations());
        return record;
    }

    public RecordCreateResponse toCreateResponse(Record record) {
        return new RecordCreateResponse(record.getId(), record.getStatus());
    }

    public SoilData toSoilData(RecordCreateRequest.SoilDataRequest request, Record record) {
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
        return data;
    }

    public WaterData toWaterData(RecordCreateRequest.WaterDataRequest request, Record record) {
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
        return data;
    }

    public FoliarData toFoliarData(RecordCreateRequest.FoliarDataRequest request, Record record) {
        FoliarData data = new FoliarData();
        data.setRecord(record);
        data.setNitrogen(request.nitrogen());
        data.setPhosphorus(request.phosphorus());
        data.setPotassium(request.potassium());
        data.setCalcium(request.calcium());
        data.setMagnesium(request.magnesium());
        data.setSymptoms(request.symptoms());
        return data;
    }

    public List<FoliarPhoto> toFoliarPhotos(List<RecordCreateRequest.FoliarPhotoRequest> requests,
                                             Record record) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream().map(request -> {
            FoliarPhoto photo = new FoliarPhoto();
            photo.setId(request.id());
            photo.setRecord(record);
            photo.setPath(request.path());
            return photo;
        }).toList();
    }
}
