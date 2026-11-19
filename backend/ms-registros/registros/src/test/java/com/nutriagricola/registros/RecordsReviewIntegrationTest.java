package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.dto.ReviewCreateRequest;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import com.nutriagricola.registros.service.RecordsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class RecordsReviewIntegrationTest {
    @Autowired
    private RecordsService recordsService;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private RecordReviewRepository reviewRepository;

    @Autowired
    private FarmUserRepository farmUsers;

    @Test
    void savesReviewAndUpdatesRecordStatusInOneTransaction() {
        String reviewId = UUID.randomUUID().toString();

        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName("Predio de prueba");
        farm.setLocation("Chile");
        farm = farmRepository.save(farm);

        String advisorId = UUID.randomUUID().toString();
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(advisorId);
        farmUsers.save(assignment);

        Crop crop = new Crop();
        crop.setId(UUID.randomUUID().toString());
        crop.setFarm(farm);
        crop.setName("Tomate");
        crop = cropRepository.save(crop);

        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId("farmer-test");
        record.setDate("2026-11-17");
        record.setType("SOIL");
        record.setStatus("PENDING");
        record = recordRepository.save(record);

        recordsService.createReview(new ReviewCreateRequest(
                reviewId,
                record.getId(),
                "2026-11-17T12:00:00",
                "VALIDATED",
                "Revisión válida"), new UserClientResponse(advisorId, "ADVISOR", true, "Asesor", "Prueba"));

        reviewRepository.flush();

        assertNotNull(reviewRepository.findById(reviewId).orElse(null));
        assertEquals(advisorId, reviewRepository.findById(reviewId).orElseThrow().getAdvisorId());
        assertEquals("VALIDATED", recordRepository.findById(record.getId()).orElseThrow().getStatus());
    }
}
