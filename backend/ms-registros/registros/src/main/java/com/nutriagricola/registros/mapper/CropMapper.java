package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.CropResponse;
import com.nutriagricola.registros.dto.CropCreateRequest;
import com.nutriagricola.registros.dto.CropUpdateRequest;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {
    public Crop toEntity(CropCreateRequest request, Farm farm) {
        Crop crop = new Crop();
        crop.setId(request.id());
        crop.setFarm(farm);
        crop.setName(request.name().strip());
        crop.setVariety(request.variety());
        crop.setAreaHectares(request.areaHectares());
        crop.setPhenologicalStage(request.phenologicalStage());
        crop.setImagePath(request.imagePath());
        return crop;
    }

    public void updateEntity(CropUpdateRequest request, Crop crop) {
        crop.setName(request.name().strip());
        crop.setVariety(request.variety());
        crop.setAreaHectares(request.areaHectares());
        crop.setPhenologicalStage(request.phenologicalStage());
        crop.setImagePath(request.imagePath());
    }

    public CropResponse toResponse(Crop crop) {
        return new CropResponse(crop.getId(), crop.getFarm().getId(), crop.getName(),
                crop.getVariety(), crop.getAreaHectares(), crop.getPhenologicalStage(),
                crop.getImagePath(), crop.getActive());
    }
}
