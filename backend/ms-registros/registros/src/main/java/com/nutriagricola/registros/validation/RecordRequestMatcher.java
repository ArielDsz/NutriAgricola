package com.nutriagricola.registros.validation;

import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.dto.RecordDetailResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class RecordRequestMatcher {
    public boolean matches(RecordCreateRequest request, RecordDetailResponse saved) {
        if (!Objects.equals(request.id(), saved.id())
                || !Objects.equals(request.cropId(), saved.cropId())
                || !Objects.equals(request.date().toString(), saved.date())
                || !Objects.equals(request.type().strip().toUpperCase(Locale.ROOT), saved.type())
                || !Objects.equals(request.observations(), saved.observations())) {
            return false;
        }

        var soil = saved.soilData();
        var savedSoil = soil == null ? null : new RecordCreateRequest.SoilDataRequest(
                soil.ph(), soil.electricalConductivity(), soil.nitrogen(), soil.phosphorus(),
                soil.potassium(), soil.calcium(), soil.magnesium(), soil.organicMatter());
        var water = saved.waterData();
        var savedWater = water == null ? null : new RecordCreateRequest.WaterDataRequest(
                water.ph(), water.electricalConductivity(), water.nitrogen(), water.phosphorus(),
                water.potassium(), water.calcium(), water.magnesium(), water.waterSource());
        var foliar = saved.foliarData();
        var savedFoliar = foliar == null ? null : new RecordCreateRequest.FoliarDataRequest(
                foliar.nitrogen(), foliar.phosphorus(), foliar.potassium(), foliar.calcium(),
                foliar.magnesium(), foliar.symptoms());

        List<RecordCreateRequest.FoliarPhotoRequest> requestedPhotos = request.foliarPhotos() == null
                ? List.of() : request.foliarPhotos();
        var photoOrder = Comparator.comparing(RecordCreateRequest.FoliarPhotoRequest::id);
        var savedPhotos = saved.foliarPhotos().stream()
                .map(photo -> new RecordCreateRequest.FoliarPhotoRequest(photo.id(), photo.path()))
                .sorted(photoOrder).toList();

        return Objects.equals(request.soilData(), savedSoil)
                && Objects.equals(request.waterData(), savedWater)
                && Objects.equals(request.foliarData(), savedFoliar)
                && requestedPhotos.stream().sorted(photoOrder).toList().equals(savedPhotos);
    }
}
