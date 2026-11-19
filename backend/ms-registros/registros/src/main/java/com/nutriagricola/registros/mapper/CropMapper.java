package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.model.Crop;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {
    public CropResponse toResponse(Crop crop) {
        return new CropResponse(crop.getId(), crop.getFarm().getId(), crop.getName(),
                crop.getVariety(), crop.getAreaHectares(), crop.getPhenologicalStage(),
                crop.getImagePath(), crop.getActive());
    }
}
