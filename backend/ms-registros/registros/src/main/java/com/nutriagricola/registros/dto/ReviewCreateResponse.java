package com.nutriagricola.registros.dto;

public record ReviewCreateResponse(String id, String recordId, String advisorId, String advisorName,
                                   String date, String result, String comment, String recordStatus) { }
