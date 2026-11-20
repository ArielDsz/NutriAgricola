package com.nutriagricola.registros.dto;
import java.util.List;
public record ProgramPageResponse(List<ProgramSummaryResponse> content, int page, int size,
                                   long totalElements, int totalPages) { }
