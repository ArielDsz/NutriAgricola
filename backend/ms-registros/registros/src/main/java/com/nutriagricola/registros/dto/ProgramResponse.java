package com.nutriagricola.registros.dto;
import java.time.LocalDateTime;
public record ProgramResponse(String id, String cropId, String advisorId, String advisorName,
                               ProgramContent content, boolean active, LocalDateTime createdAt,
                               String species, String plot) { }
