package com.nutriagricola.registros.dto;

public record RecordSummaryResponse(String id, String cropId, String userId, String date,
                                   String type, String status, String observations) { }
