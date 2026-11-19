package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.model.Crop;
import com.nutriagricola.registros.model.Farm;
import com.nutriagricola.registros.model.FarmUser;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.repository.CropRepository;
import com.nutriagricola.registros.repository.FarmRepository;
import com.nutriagricola.registros.repository.FarmUserRepository;
import com.nutriagricola.registros.repository.FoliarPhotoRepository;
import com.nutriagricola.registros.repository.RecordRepository;
import com.nutriagricola.registros.repository.RecordReviewRepository;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordValidationIntegrationTest {
    private static final String AUTHORIZATION = "Bearer token-de-prueba";

    @Autowired private MockMvc mvc;
    @Autowired private FarmRepository farms;
    @Autowired private FarmUserRepository assignments;
    @Autowired private CropRepository crops;
    @Autowired private RecordRepository records;
    @Autowired private RecordReviewRepository reviews;
    @Autowired private FoliarPhotoRepository photos;

    @MockitoBean
    private UsersClient usersClient;

    @Test
    void rejectsEmptyAnalysesMixedDataAndNonFiniteMeasurements() throws Exception {
        Crop crop = assignedCrop(authenticate("FARMER").id());
        for (String data : List.of("\"soilData\":{}", "\"soilData\":{\"ph\":1e999}",
                "\"soilData\":{\"ph\":6},\"waterData\":{\"ph\":7}")) {
            String id = UUID.randomUUID().toString();
            mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                            .contentType(MediaType.APPLICATION_JSON).content(recordBody(id, crop.getId(), "SOIL", data)))
                    .andExpect(status().isBadRequest());
            assertFalse(records.existsById(id));
        }
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(),
                                crop.getId(), "FOLIAR", "\"foliarData\":{}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsZeroAndTheMinimumTextDataSupportedByAndroid() throws Exception {
        Crop crop = assignedCrop(authenticate("FARMER").id());
        String soilId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(soilId, crop.getId(),
                                "SOIL", "\"soilData\":{\"ph\":0}")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(),
                                crop.getId(), "WATER", "\"waterData\":{\"waterSource\":\"Pozo\"}")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(),
                                crop.getId(), "FOLIAR", "\"foliarData\":{\"symptoms\":\"Clorosis\"}")))
                .andExpect(status().isCreated());
        records.flush();
        assertEquals("2026-11-19", records.findById(soilId).orElseThrow().getDate());
    }

    @Test
    void rejectsInvalidIdentifiersAndImpossibleDates() throws Exception {
        Crop crop = assignedCrop(authenticate("FARMER").id());
        String valid = recordBody(UUID.randomUUID().toString(), crop.getId(), "SOIL", "\"soilData\":{\"ph\":6}");
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(valid.replace("2026-11-19", "2026-02-30")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody("incorrecto", crop.getId(),
                                "SOIL", "\"soilData\":{\"ph\":6}")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.id").exists());
        authenticate("ADVISOR");
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":"%s","recordId":"%s","date":"2026-02-30T12:00:00","result":"VALIDATED"}
                                """.formatted(UUID.randomUUID().toString(), UUID.randomUUID().toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alignsObservedAndValidatedReviewsWithAndroidAndProtectsUnavailableRecords() throws Exception {
        UserClientResponse advisor = authenticate("ADVISOR");
        Crop crop = assignedCrop(advisor.id());
        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setCrop(crop);
        record.setUserId(UUID.randomUUID().toString());
        record.setDate("2026-11-19");
        record.setType("SOIL");
        record.setStatus("PENDING");
        record = records.saveAndFlush(record);

        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(record.getId(), "OBSERVED", " ")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(record.getId(), "OBSERVED", " Revisar datos ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordStatus").value("PENDING"))
                .andExpect(jsonPath("$.comment").value("Revisar datos"));
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(record.getId(), "VALIDATED", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recordStatus").value("VALIDATED"))
                .andExpect(jsonPath("$.comment").value(""));
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(record.getId(), "VIEWED", "Comentario")))
                .andExpect(status().isBadRequest());
        record.setStatus("DELETED");
        records.saveAndFlush(record);
        mvc.perform(post("/api/v1/reviews").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(reviewBody(record.getId(), "VALIDATED", null)))
                .andExpect(status().isConflict());
        assertEquals(2, reviews.findByRecord_IdOrderByDateDescIdAsc(record.getId()).size());
        assertEquals("DELETED", records.findById(record.getId()).orElseThrow().getStatus());
    }

    @Test
    void rejectsRepeatedOrReusedPhotoIdentifiersWithoutOverwritingPhotos() throws Exception {
        Crop crop = assignedCrop(authenticate("FARMER").id());
        String photoId = UUID.randomUUID().toString();
        String data = "\"foliarData\":{},\"foliarPhotos\":[{\"id\":\"" + photoId + "\",\"path\":\"original.jpg\"}]";
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(), crop.getId(), "FOLIAR", data)))
                .andExpect(status().isCreated());
        photos.flush();
        String rejectedId = UUID.randomUUID().toString();
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(rejectedId, crop.getId(), "FOLIAR",
                                data.replace("original.jpg", "reemplazo.jpg"))))
                .andExpect(status().isConflict());
        assertFalse(records.existsById(rejectedId));
        assertEquals("original.jpg", photos.findById(photoId).orElseThrow().getPath());

        String repeated = "\"foliarData\":{},\"foliarPhotos\":[{\"id\":\"%s\",\"path\":\"a.jpg\"},{\"id\":\"%s\",\"path\":\"b.jpg\"}]"
                .formatted(photoId, photoId);
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(), crop.getId(), "FOLIAR", repeated)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/records").header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(UUID.randomUUID().toString(), crop.getId(),
                                "FOLIAR", "\"foliarData\":{},\"foliarPhotos\":[null]")))
                .andExpect(status().isBadRequest());
    }

    private UserClientResponse authenticate(String role) {
        UserClientResponse user = new UserClientResponse(UUID.randomUUID().toString(), role,
                true, "Usuario", "Prueba");
        when(usersClient.getCurrentUser(AUTHORIZATION)).thenReturn(user);
        return user;
    }

    private Crop assignedCrop(String userId) {
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

    private String recordBody(String id, String cropId, String type, String data) {
        return """
                {"id":"%s","cropId":"%s","date":"2026-11-19","type":"%s",%s}
                """.formatted(id, cropId, type, data);
    }

    private String reviewBody(String recordId, String result, String comment) {
        String commentField = comment == null ? "" : ",\"comment\":\"" + comment + "\"";
        return """
                {"id":"%s","recordId":"%s","date":"2026-11-19T12:00:00","result":"%s"%s}
                """.formatted(UUID.randomUUID().toString(), recordId, result, commentField);
    }
}
