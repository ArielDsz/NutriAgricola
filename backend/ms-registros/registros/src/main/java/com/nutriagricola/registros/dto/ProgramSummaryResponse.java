package com.nutriagricola.registros.dto;
import java.time.LocalDate;
public record ProgramSummaryResponse(String id, String cropId, String advisorId, String name,
                                      String season, LocalDate startDate, LocalDate endDate, boolean active) { }
