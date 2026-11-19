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
class RecordRetryIntegrationTest {
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
    void confirmsRetryWithoutCreatingAnotherRecord(String type) throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = UUID.randomUUID().toString();
        long countBefore = records.count();
        String original = body(id, crop.getId(), type, false);

        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();

        String retry = body(id, crop.getId(), type, true)
                .replace("\"type\":\"" + type + "\"", "\"type\":\"" + type.toLowerCase(java.util.Locale.ROOT) + "\"");
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(retry))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("PENDING"));
        assertEquals(countBefore + 1, records.count());
    }

    @Test
    void rejectsDifferentPayloadsAndDoesNotOverwriteAnEditedRecord() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = UUID.randomUUID().toString();
        String original = body(id, crop.getId(), "FOLIAR", false);
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isCreated());
        String otherCropId = assignedCrop(authorId).getId();

        for (String different : List.of(
                original.replace("2026-11-19", "2026-11-18"),
                original.replace("Original", "Diferente"),
                original.replace("\"nitrogen\":2.5", "\"nitrogen\":3.5"),
                original.replace("a.jpg", "otra.jpg"),
                original.replace(crop.getId(), otherCropId),
                body(id, crop.getId(), "SOIL", false))) {
            mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                            .contentType(MediaType.APPLICATION_JSON).content(different))
                    .andExpect(status().isConflict());
        }

        mvc.perform(put("/api/v1/records/" + id).header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"date":"2026-11-19","observations":"Editado","foliarData":{"nitrogen":4}}
                                """))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isConflict());
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/api/v1/records/" + id).header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observations").value("Editado"))
                .andExpect(jsonPath("$.foliarData.nitrogen").value(4.0))
                .andExpect(jsonPath("$.foliarPhotos.length()").value(2));
    }

    @Test
    void confirmsPreviousUploadWithoutResettingReviewOrDeletionStatus() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = UUID.randomUUID().toString();
        String original = body(id, crop.getId(), "SOIL", false);
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isCreated());
        String advisorId = UUID.randomUUID().toString();
        assign(crop.getFarm(), advisorId);
        authenticate(advisorId, "ADVISOR");
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":"%s","recordId":"%s","date":"2026-11-19T12:00:00","result":"VALIDATED"}
                                """.formatted(UUID.randomUUID().toString(), id)))
                .andExpect(status().isCreated());

        authenticate(authorId, "FARMER");
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VALIDATED"));
        mvc.perform(delete("/api/v1/records/" + id).header("Authorization", AUTHORIZATION))
                .andExpect(status().isNoContent());
        crop.setActive(false);
        crops.saveAndFlush(crop);
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DELETED"));
        assertEquals("DELETED", records.findById(id).orElseThrow().getStatus());
        assertEquals(1, reviews.findByRecord_IdOrderByDateDescIdAsc(id).size());
    }

    @Test
    void rejectsRetryFromAnotherResponsibleAndAuthorWithoutAssignment() throws Exception {
        String authorId = UUID.randomUUID().toString();
        authenticate(authorId, "FARMER");
        Crop crop = assignedCrop(authorId);
        String id = UUID.randomUUID().toString();
        String original = body(id, crop.getId(), "SOIL", false);
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isCreated());
        String otherId = UUID.randomUUID().toString();
        assign(crop.getFarm(), otherId);
        authenticate(otherId, "FARMER");
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isForbidden());
        assignments.findAll().stream().filter(assignment -> authorId.equals(assignment.getUserId()))
                .forEach(assignments::delete);
        assignments.flush();
        authenticate(authorId, "FARMER");
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isForbidden());
        assertEquals(authorId, records.findById(id).orElseThrow().getUserId());
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

    private String body(String id, String cropId, String type, boolean reversePhotos) {
        String firstPhoto = "{\"id\":\"11111111-1111-4111-8111-111111111111\",\"path\":\"a.jpg\"}";
        String secondPhoto = "{\"id\":\"22222222-2222-4222-8222-222222222222\",\"path\":\"b.jpg\"}";
        String data = switch (type) {
            case "SOIL" -> "\"soilData\":{\"ph\":6.5,\"nitrogen\":0}";
            case "WATER" -> "\"waterData\":{\"ph\":7.1,\"waterSource\":\"Pozo\"}";
            default -> "\"foliarData\":{\"nitrogen\":2.5,\"symptoms\":\"Clorosis\"},\"foliarPhotos\":["
                    + (reversePhotos ? secondPhoto + "," + firstPhoto : firstPhoto + "," + secondPhoto) + "]";
        };
        return """
                {"id":"%s","cropId":"%s","date":"2026-11-19","type":"%s","observations":"Original",%s}
                """.formatted(id, cropId, type, data);
    }
}
