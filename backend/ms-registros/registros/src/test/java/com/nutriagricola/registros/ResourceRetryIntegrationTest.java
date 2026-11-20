package com.nutriagricola.registros;

import com.nutriagricola.registros.client.*;
import com.nutriagricola.registros.model.*;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class ResourceRetryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FarmRepository farms;
    @Autowired FarmUserRepository assignments;
    @Autowired CropRepository crops;
    @Autowired RecordRepository records;
    @Autowired RecordReviewRepository reviews;
    @MockitoBean UsersClient usersClient;

    @Test
    void confirmsFarmAndCropRetriesAndRejectsChangedContent() throws Exception {
        auth(UUID.randomUUID().toString(), "FARMER");
        String farmId = UUID.randomUUID().toString(), cropId = UUID.randomUUID().toString();
        String farm = "{\"id\":\"" + farmId + "\",\"name\":\"Predio\",\"location\":\"Chile\"}";
        String crop = "{\"id\":\"" + cropId + "\",\"farmId\":\"" + farmId + "\",\"name\":\"Uva\",\"areaHectares\":2}";
        postBody("/api/v1/farms", farm, 201); postBody("/api/v1/farms", farm, 200);
        postBody("/api/v1/farms", farm.replace("Predio", "Diferente"), 409);
        postBody("/api/v1/crops", crop, 201); postBody("/api/v1/crops", crop, 200);
        postBody("/api/v1/crops", crop.replace("\"areaHectares\":2", "\"areaHectares\":3"), 409);
        mvc.perform(delete("/api/v1/farms/" + farmId).header("Authorization", "Bearer demo")).andExpect(status().isNoContent());
        postBody("/api/v1/farms", farm, 200); postBody("/api/v1/crops", crop, 200);
        assertEquals(1, assignments.findAll().size());
    }

    @Test
    void repeatedReviewDoesNotOverrideLaterDecisionAndRejectsOtherAuthors() throws Exception {
        String advisor = UUID.randomUUID().toString(); auth(advisor, "ADVISOR");
        Farm farm = new Farm(); farm.setId(UUID.randomUUID().toString()); farm.setName("Predio"); farm.setLocation("Chile");
        farm = farms.save(farm); assign(farm, advisor);
        Crop crop = new Crop(); crop.setId(UUID.randomUUID().toString()); crop.setFarm(farm); crop.setName("Uva"); crop = crops.save(crop);
        Record record = new Record(); record.setId(UUID.randomUUID().toString()); record.setCrop(crop);
        record.setUserId(UUID.randomUUID().toString()); record.setDate("2026-11-19"); record.setType("SOIL"); record.setStatus("PENDING");
        record = records.saveAndFlush(record);
        String first = review(UUID.randomUUID().toString(), record.getId(), "OBSERVED");
        postBody("/api/v1/reviews", first, 201);
        postBody("/api/v1/reviews", review(UUID.randomUUID().toString(), record.getId(), "VALIDATED"), 201);
        postBody("/api/v1/reviews", first, 200);
        assertEquals("VALIDATED", records.findById(record.getId()).orElseThrow().getStatus());
        postBody("/api/v1/reviews", first.replace("Comentario", "Diferente"), 409);
        String other = UUID.randomUUID().toString(); assign(farm, other); auth(other, "ADVISOR");
        postBody("/api/v1/reviews", first, 403);
        auth(advisor, "ADVISOR"); record.setStatus("DELETED"); records.saveAndFlush(record);
        postBody("/api/v1/reviews", first, 200);
        assertEquals("DELETED", records.findById(record.getId()).orElseThrow().getStatus());
        assertEquals(2, reviews.findByRecord_IdOrderByDateDescIdAsc(record.getId()).size());
    }

    private void auth(String id, String role) {
        when(usersClient.getCurrentUser("Bearer demo")).thenReturn(new UserClientResponse(id, role, true, "Usuario", "Prueba"));
    }
    private void assign(Farm farm, String user) {
        FarmUser a = new FarmUser(); a.setFarm(farm); a.setUserId(user); assignments.save(a);
    }
    private void postBody(String url, String body, int expected) throws Exception {
        mvc.perform(post(url).header("Authorization", "Bearer demo").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expected));
    }
    private String review(String id, String record, String result) {
        return """
                {"id":"%s","recordId":"%s","date":"2026-11-19T12:00:00","result":"%s","comment":"Comentario"}
                """.formatted(id, record, result);
    }
}
