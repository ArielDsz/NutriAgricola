package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.service.RecordsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FarmController {
    private final RecordsService recordsService;

    public FarmController(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @GetMapping("/farms")
    public List<FarmResponse> getFarms() {
        return recordsService.getFarms();
    }

    @GetMapping("/crops/farm/{farmId}")
    public List<CropResponse> getCropsByFarm(@PathVariable String farmId) {
        return recordsService.getCropsByFarm(farmId);
    }
}
