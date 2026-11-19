package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordUpdateIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;
    @Autowired private RecordReviewRepository reviews;
    @Autowired private EntityManager entityManager;

    @MockitoBean
    private UsersClient usersClient;

    @ParameterizedTest
    @ValueSource(strings = {"SOIL", "WATER", "FOLIAR"})
    void authorEditsMeasurementsWithoutChangingIdentityTypeOrPhotos(String type) throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = createRecord(crop.getId(), type);
        String data = switch (type) {
            case "SOIL" -> "\"soilData\":{\"ph\":0}";
            case "WATER" -> "\"waterData\":{\"ph\":0,\"waterSource\":\"Canal\"}";
            default -> "\"foliarData\":{\"nitrogen\":0,\"symptoms\":\"Actualizado\"}";
        };
        String body = """
                {"date":"2026-11-18","observations":"Corregido",%s,
                 "userId":"ajeno","cropId":"otro-cultivo","type":"OTRO","status":"VALIDATED"}
                """.formatted(data);

        mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cropId").value(crop.getId()))
                .andExpect(jsonPath("$.userId").value(authorId))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.date").value("2026-11-18"));
        entityManager.flush();
        entityManager.clear();
        var detail = mvc.perform(get("/api/v1/records/" + id).header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk()).andExpect(jsonPath("$.observations").value("Corregido"));
        switch (type) {
            case "SOIL" -> detail.andExpect(jsonPath("$.soilData.ph").value(0.0));
            case "WATER" -> detail.andExpect(jsonPath("$.waterData.ph").value(0.0))
                    .andExpect(jsonPath("$.waterData.waterSource").value("Canal"));
            case "FOLIAR" -> detail.andExpect(jsonPath("$.foliarData.nitrogen").value(0.0))
                    .andExpect(jsonPath("$.foliarPhotos.length()").value(1))
                    .andExpect(jsonPath("$.foliarPhotos[0].path").value("original.jpg"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"OBSERVED", "VALIDATED"})
    void rejectsEditingAfterFirstReviewEvenWhenStatusIsPending(String result) throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = createRecord(crop.getId(), "SOIL");
        String advisorId = UUID.randomUUID().toString();
        assign(crop.getFarm(), advisorId);
        authenticate(advisorId, "ADVISOR");
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":"%s","recordId":"%s","date":"2026-11-19T12:00:00",
                                 "result":"%s","comment":"Revisión de prueba"}
                                """.formatted(UUID.randomUUID().toString(), id, result)))
                .andExpect(status().isCreated());
        authenticate(authorId, "FARMER");

        mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody("\"soilData\":{\"ph\":0}")))
                .andExpect(status().isConflict());
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/api/v1/records/" + id).header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-19"))
                .andExpect(jsonPath("$.soilData.ph").value(6.5));
        assertEquals(1, reviews.findByRecord_IdOrderByDateDescIdAsc(id).size());
    }

    @Test
    void rejectsOtherResponsibleAndAdvisorEvenOnSharedFarm() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = createRecord(crop.getId(), "SOIL");
        String otherId = UUID.randomUUID().toString();
        assign(crop.getFarm(), otherId);
        for (String role : List.of("FARMER", "ADVISOR")) {
            authenticate(otherId, role);
            mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                            .contentType(MediaType.APPLICATION_JSON).content(updateBody("\"soilData\":{\"ph\":0}")))
                    .andExpect(status().isForbidden());
        }
        assertEquals(authorId, records.findById(id).orElseThrow().getUserId());
    }

    @Test
    void rejectsInvalidUpdatesAndEditingAnAnnulledRecord() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        String id = createRecord(assignedCrop(authorId).getId(), "SOIL");
        for (String data : List.of("\"soilData\":{}", "\"waterData\":{\"ph\":7}",
                "\"soilData\":{\"ph\":1e999}")) {
            mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                            .contentType(MediaType.APPLICATION_JSON).content(updateBody(data)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("\"soilData\":{\"ph\":7}").replace("2026-11-18", "2026-02-30")))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/records/" + id).header("Authorization", AUTHORIZATION))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody("\"soilData\":{\"ph\":7}")))
                .andExpect(status().isConflict());
        assertEquals("DELETED", records.findById(id).orElseThrow().getStatus());
    }

    private void authenticate(String userId, String role) {
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(
                new UserClientResponse(userId, role, true, "Usuario", "Prueba"));
    }

    private Crop assignedCrop(String authorId) {
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
        return crops.save(crop);
    }

    private void assign(Farm farm, String userId) {
        FarmUser assignment = new FarmUser();
        assignment.setFarm(farm);
        assignment.setUserId(userId);
        assignments.save(assignment);
    }

    private String createRecord(String cropId, String type) throws Exception {
        String id = UUID.randomUUID().toString();
        String data = switch (type) {
            case "SOIL" -> "\"soilData\":{\"ph\":6.5}";
            case "WATER" -> "\"waterData\":{\"ph\":7.1,\"waterSource\":\"Pozo\"}";
            default -> "\"foliarData\":{\"nitrogen\":2.5},\"foliarPhotos\":[{\"id\":\""
                    + UUID.randomUUID() + "\",\"path\":\"original.jpg\"}]";
        };
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":"%s","cropId":"%s","date":"2026-11-19","type":"%s",%s}
                                """.formatted(id, cropId, type, data)))
                .andExpect(status().isCreated());
        records.flush();
        return id;
    }

    private String updateBody(String data) {
        return """
                {"date":"2026-11-18","observations":"Corregido",%s}
                """.formatted(data);
    }
}
