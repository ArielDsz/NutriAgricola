package com.nutriagricola.registros.dto;

import java.util.List;

public record RecordPageResponse(List<RecordSummaryResponse> content, int page, int size,
                                 long totalElements, int totalPages) { }
