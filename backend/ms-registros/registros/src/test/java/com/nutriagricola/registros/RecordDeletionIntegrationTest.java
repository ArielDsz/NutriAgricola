package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.model.FoliarData;
import com.nutriagricola.registros.model.FoliarPhoto;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.model.RecordReview;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarDataRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordDeletionIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;
    @Autowired private FoliarDataRepository foliarData;
    @Autowired private FoliarPhotoRepository photos;
    @Autowired private RecordReviewRepository reviews;
    @Autowired private EntityManager entityManager;

    @MockitoBean
    private UsersClient usersClient;

    @Test
    void authorCanAnnulRepeatedlyWithoutLosingMeasurementsPhotosOrReviews() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Record record = saveRecord(authorId);
        FoliarData data = new FoliarData();
        data.setRecord(record);
        data.setNitrogen(2.5);
        foliarData.save(data);
        FoliarPhoto photo = new FoliarPhoto();
        photo.setId(UUID.randomUUID().toString());
        photo.setRecord(record);
        photo.setPath("demo/photo.jpg");
        photos.save(photo);
        RecordReview review = new RecordReview();
        review.setId(UUID.randomUUID().toString());
        review.setRecord(record);
        review.setAdvisorId(UUID.randomUUID().toString());
        review.setAdvisorName("Asesor de prueba");
        review.setDate("2026-11-19T12:00:00");
        review.setResult("VALIDATED");
        review.setComment("Revisión anterior");
        reviews.save(review);
        entityManager.flush();
        entityManager.clear();

        String path = "/api/v1/records/" + record.getId();
        mvc.perform(delete(path).header("Authorization", AUTHORIZATION)).andExpect(status().isNoContent());
        mvc.perform(delete(path).header("Authorization", AUTHORIZATION)).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        assertEquals("DELETED", records.findById(record.getId()).orElseThrow().getStatus());
        assertEquals(2.5, foliarData.findById(record.getId()).orElseThrow().getNitrogen());
        assertEquals("demo/photo.jpg", photos.findById(photo.getId()).orElseThrow().getPath());
        assertTrue(reviews.existsById(review.getId()));
        mvc.perform(get(path).header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DELETED"))
                .andExpect(jsonPath("$.reviews.length()").value(1));
        mvc.perform(get("/api/v1/records").param("status", "DELETED")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(record.getId()));
    }

    @Test
    void rejectsOtherResponsibleAndAdvisorEvenWhenAssignedToSameFarm() throws Exception {
        Record record = saveRecord(UUID.randomUUID().toString());
        String farmId = record.getCrop().getFarm().getId();
        String otherId = UUID.randomUUID().toString();
        assign(farms.findById(farmId).orElseThrow(), otherId);

        authenticate(otherId, "FARMER");
        mvc.perform(delete("/api/v1/records/" + record.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
        authenticate(otherId, "ADVISOR");
        mvc.perform(delete("/api/v1/records/" + record.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
        assertEquals("VALIDATED", records.findById(record.getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectsAuthorWithoutCurrentFarmAssignmentAndReportsMissingRecord() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Record record = saveRecord(authorId);
        assignments.deleteAll();
        assignments.flush();

        mvc.perform(delete("/api/v1/records/" + record.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
        assertEquals("VALIDATED", records.findById(record.getId()).orElseThrow().getStatus());
        mvc.perform(delete("/api/v1/records/" + UUID.randomUUID()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isNotFound());
    }

    private void authenticate(String userId, String role) {
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(
                new UserClientResponse(userId, role, true, "Usuario", "Prueba"));
    }

    private Record saveRecord(String authorId) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName("Predio de prueba");
        farm.setLocation("Chile");
        farm = farms.save(farm);
        assign(farm, authorId);
        Crop crop = new Crop();
        crop.setId(UUID.randomUUID().toString());
        crop.setFarm(farm);
        crop.setName("Uva de mesa");
        crop = crops.save(crop);
        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId(authorId);
        record.setDate("2026-11-19");
        record.setType("FOLIAR");
        record.setStatus("VALIDATED");
        return records.saveAndFlush(record);
    }

    private void assign(Farm farm, String userId) {
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
    }
}
