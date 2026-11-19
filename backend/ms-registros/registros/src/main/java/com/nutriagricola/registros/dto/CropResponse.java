package com.nutriagricola.registros.dto;

public record CropResponse(String id, String farmId, String name, String variety, Double areaHectares,
                           String phenologicalStage, String imagePath, Boolean active) { }
