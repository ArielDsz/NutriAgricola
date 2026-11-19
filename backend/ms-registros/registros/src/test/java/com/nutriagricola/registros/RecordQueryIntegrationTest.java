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
import com.nutriagricola.registros.model.SoilData;
import com.nutriagricola.registros.model.WaterData;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarDataRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import com.nutriagricola.registros.repository.SoilDataRepository;
import com.nutriagricola.registros.repository.WaterDataRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordQueryIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;
    @Autowired private SoilDataRepository soilData;
    @Autowired private WaterDataRepository waterData;
    @Autowired private FoliarDataRepository foliarData;
    @Autowired private FoliarPhotoRepository photos;
    @Autowired private RecordReviewRepository reviews;
    @Autowired private EntityManager entityManager;

    @MockitoBean
    private UsersClient usersClient;

    @Test
    void paginatesFiltersAndExcludesOtherUsersFarms() throws Exception {
        UserClientResponse user = authenticate("FARMER");
        Crop crop = saveCrop(user.id());
        Record oldest = saveRecord(crop, "SOIL", "PENDING", "2026-11-17");
        Record middle = saveRecord(crop, "WATER", "PENDING", "2026-11-18");
        Record newest = saveRecord(crop, "SOIL", "VALIDATED", "2026-11-19");
        saveRecord(saveCrop(UUID.randomUUID().toString()), "SOIL", "PENDING", "2026-11-20");
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/v1/records").param("size", "2").header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(newest.getId()))
                .andExpect(jsonPath("$.content[1].id").value(middle.getId()))
                .andExpect(jsonPath("$.content[0].soilData").doesNotExist());
        mvc.perform(get("/api/v1/records").param("page", "1").param("size", "2")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oldest.getId()));
        mvc.perform(get("/api/v1/records").param("cropId", crop.getId())
                        .param("type", "soil").param("status", "validated")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(newest.getId()));
        mvc.perform(get("/api/v1/records/status/PENDING").param("size", "1").param("page", "1")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(oldest.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOIL", "WATER", "FOLIAR"})
    void returnsMeasurementsAndReviewsForTheRequestedType(String type) throws Exception {
        UserClientResponse advisor = authenticate("ADVISOR");
        Record record = saveRecord(saveCrop(advisor.id()), type, "VALIDATED", "2026-11-19");
        switch (type) {
            case "SOIL" -> {
                SoilData data = new SoilData();
                data.setRecord(record);
                data.setPh(0.0);
                data.setOrganicMatter(1.2);
                soilData.save(data);
            }
            case "WATER" -> {
                WaterData data = new WaterData();
                data.setRecord(record);
                data.setPh(7.1);
                data.setWaterSource("Pozo");
                waterData.save(data);
            }
            case "FOLIAR" -> {
                FoliarData data = new FoliarData();
                data.setRecord(record);
                data.setNitrogen(0.0);
                data.setSymptoms("Clorosis");
                foliarData.save(data);
                FoliarPhoto photo = new FoliarPhoto();
                photo.setId(UUID.randomUUID().toString());
                photo.setRecord(record);
                photo.setPath("demo/photo.jpg");
                photos.save(photo);
            }
        }
        RecordReview review = new RecordReview();
        review.setId(UUID.randomUUID().toString());
        review.setRecord(record);
        review.setAdvisorId(advisor.id());
        review.setAdvisorName("Asesor Prueba");
        review.setDate("2026-11-19T12:00:00");
        review.setResult("VALIDATED");
        review.setComment("Revisión de prueba");
        reviews.save(review);
        entityManager.flush();
        entityManager.clear();

        var response = mvc.perform(get("/api/v1/records/" + record.getId())
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(record.getId()))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.reviews.length()").value(1))
                .andExpect(jsonPath("$.reviews[0].advisorId").value(advisor.id()));
        switch (type) {
            case "SOIL" -> response.andExpect(jsonPath("$.soilData.ph").value(0.0))
                    .andExpect(jsonPath("$.soilData.organicMatter").value(1.2))
                    .andExpect(jsonPath("$.waterData").doesNotExist())
                    .andExpect(jsonPath("$.foliarPhotos.length()").value(0));
            case "WATER" -> response.andExpect(jsonPath("$.waterData.ph").value(7.1))
                    .andExpect(jsonPath("$.waterData.waterSource").value("Pozo"))
                    .andExpect(jsonPath("$.soilData").doesNotExist());
            case "FOLIAR" -> response.andExpect(jsonPath("$.foliarData.nitrogen").value(0.0))
                    .andExpect(jsonPath("$.foliarData.symptoms").value("Clorosis"))
                    .andExpect(jsonPath("$.foliarPhotos[0].path").value("demo/photo.jpg"))
                    .andExpect(jsonPath("$.soilData").doesNotExist());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"FARMER", "ADVISOR"})
    void rejectsOtherFarmsDetailsAndCropFilters(String role) throws Exception {
        authenticate(role);
        Crop other = saveCrop(UUID.randomUUID().toString());
        Record record = saveRecord(other, "SOIL", "PENDING", "2026-11-19");

        mvc.perform(get("/api/v1/records/" + record.getId()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/records").param("cropId", other.getId())
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/records/" + UUID.randomUUID()).header("Authorization", AUTHORIZATION))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidPaginationAndFilters() throws Exception {
        authenticate("FARMER");
        for (String query : List.of("page=-1", "size=0", "size=101", "page=texto", "type=INVALID", "status=INVALID")) {
            mvc.perform(get("/api/v1/records?" + query).header("Authorization", AUTHORIZATION))
                    .andExpect(status().isBadRequest());
        }
    }

    private UserClientResponse authenticate(String role) {
        UserClientResponse user = new UserClientResponse(UUID.randomUUID().toString(), role,
                true, "Asesor", "Prueba");
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(user);
        return user;
    }

    private Crop saveCrop(String userId) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName("Predio de prueba");
        farm.setLocation("Chile");
        farm = farms.save(farm);
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
        Crop crop = new Crop();
        crop.setId(UUID.randomUUID().toString());
        crop.setFarm(farm);
        crop.setName("Uva de mesa");
        return crops.save(crop);
    }

    private Record saveRecord(Crop crop, String type, String status, String date) {
        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId(UUID.randomUUID().toString());
        record.setDate(date);
        record.setType(type);
        record.setStatus(status);
        return records.saveAndFlush(record);
    }
}
