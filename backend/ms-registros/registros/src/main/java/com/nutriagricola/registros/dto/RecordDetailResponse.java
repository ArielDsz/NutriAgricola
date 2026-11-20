package com.nutriagricola.registros.dto;

import java.util.List;

public record RecordDetailResponse(
        String id, String cropId, String userId, String date, String type, String status,
        String observations, SoilDataResponse soilData, WaterDataResponse waterData,
        FoliarDataResponse foliarData, List<PhotoResponse> foliarPhotos,
        List<ReviewResponse> reviews) {

    public record SoilDataResponse(Double ph, Double electricalConductivity, Double nitrogen,
                                    Double phosphorus, Double potassium, Double calcium,
                                    Double magnesium, Double organicMatter) { }

    public record WaterDataResponse(Double ph, Double electricalConductivity, Double nitrogen,
                                     Double phosphorus, Double potassium, Double calcium,
                                     Double magnesium, String waterSource) { }

    public record FoliarDataResponse(Double nitrogen, Double phosphorus, Double potassium,
                                      Double calcium, Double magnesium, String symptoms) { }

    public record ReviewResponse(String id, String advisorId, String advisorName,
                                  String date, String result, String comment) { }
}
