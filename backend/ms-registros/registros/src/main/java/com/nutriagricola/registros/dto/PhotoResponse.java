package com.nutriagricola.registros.dto;

public record PhotoResponse(String id, String path, boolean uploaded, String contentType,
                             Long sizeBytes, String downloadUrl) { }
