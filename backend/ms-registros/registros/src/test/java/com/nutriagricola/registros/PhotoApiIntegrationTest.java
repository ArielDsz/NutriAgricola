package com.nutriagricola.registros;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.model.*;
import com.nutriagricola.registros.model.Record;
import com.nutriagricola.registros.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhotoApiIntegrationTest {
    @TempDir static Path directory;
    @DynamicPropertySource
    static void storage(DynamicPropertyRegistry registry) {
        registry.add("storage.photos.directory", () -> directory.toString());
    }

    @Autowired MockMvc mvc;
    @Autowired FarmRepository farms;
    @Autowired FarmUserRepository assignments;
    @Autowired CropRepository crops;
    @Autowired RecordRepository records;
    @Autowired FoliarPhotoRepository photos;
    @MockitoBean UsersClient usersClient;

    @Test
    void uploadsDownloadsAndConfirmsRetryWithoutChangingClientReference() throws Exception {
        String author = UUID.randomUUID().toString();
        auth(author, "FARMER");
        FoliarPhoto photo = fixture(author);
        String url = url(photo);
        byte[] bytes = png(0);
        mvc.perform(get(url).header("Authorization", "Bearer demo")).andExpect(status().isNotFound());
        upload(url, bytes, 200);
        upload(url, bytes, 200);
        upload(url, png(0xFFFFFF), 409);
        mvc.perform(get(url).header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(bytes));
        mvc.perform(get("/api/v1/records/" + photo.getRecord().getId()).header("Authorization", "Bearer demo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.foliarPhotos[0].uploaded").value(true))
                .andExpect(jsonPath("$.foliarPhotos[0].path").value("file:///android/original.png"))
                .andExpect(jsonPath("$.foliarPhotos[0].downloadUrl").value(url));
    }

    @Test
    void enforcesAuthorForUploadsAndAssignmentForDownloads() throws Exception {
        String author = UUID.randomUUID().toString();
        auth(author, "FARMER");
        FoliarPhoto photo = fixture(author);
        byte[] bytes = png(0);
        upload(url(photo), bytes, 200);
        String advisor = UUID.randomUUID().toString();
        assign(photo.getRecord().getCrop().getFarm(), advisor);
        auth(advisor, "ADVISOR");
        mvc.perform(get(url(photo)).header("Authorization", "Bearer demo")).andExpect(status().isOk());
        upload(url(photo), bytes, 403);
        auth(advisor, "FARMER");
        upload(url(photo), bytes, 403);
        auth(UUID.randomUUID().toString(), "FARMER");
        mvc.perform(get(url(photo)).header("Authorization", "Bearer demo")).andExpect(status().isForbidden());
        upload(url(photo), bytes, 403);
    }

    @Test
    void rejectsWrongRecordInvalidFilesAndNewUploadsOnAnnulledAnalyses() throws Exception {
        String author = UUID.randomUUID().toString();
        auth(author, "FARMER");
        FoliarPhoto photo = fixture(author);
        mvc.perform(get("/api/v1/records/" + UUID.randomUUID() + "/photos/" + photo.getId())
                .header("Authorization", "Bearer demo")).andExpect(status().isNotFound());
        upload(url(photo), new byte[]{1, 2, 3}, 415);
        photo.getRecord().setStatus("DELETED");
        records.saveAndFlush(photo.getRecord());
        upload(url(photo), png(0), 409);
    }

    private void upload(String url, byte[] bytes, int expected) throws Exception {
        mvc.perform(multipart(url).file(new MockMultipartFile("file", "photo.png", "image/png", bytes))
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", "Bearer demo"))
                .andExpect(status().is(expected));
    }

    private void auth(String id, String role) {
        when(usersClient.getCurrentUser("Bearer demo")).thenReturn(new UserClientResponse(id, role, true, "Usuario", "Prueba"));
    }

    private FoliarPhoto fixture(String author) {
        Farm farm = new Farm(); farm.setId(UUID.randomUUID().toString()); farm.setName("Predio"); farm.setLocation("Chile");
        farm = farms.save(farm); assign(farm, author);
        Crop crop = new Crop(); crop.setId(UUID.randomUUID().toString()); crop.setFarm(farm); crop.setName("Uva");
        crop = crops.save(crop);
        Record record = new Record(); record.setId(UUID.randomUUID().toString()); record.setCrop(crop);
        record.setUserId(author); record.setDate("2026-11-19"); record.setType("FOLIAR"); record.setStatus("PENDING");
        record = records.save(record);
        FoliarPhoto photo = new FoliarPhoto(); photo.setId(UUID.randomUUID().toString()); photo.setRecord(record);
        photo.setPath("file:///android/original.png");
        return photos.saveAndFlush(photo);
    }

    private void assign(Farm farm, String user) {
        FarmUser assignment = new FarmUser(); assignment.setFarm(farm); assignment.setUserId(user); assignments.save(assignment);
    }

    private String url(FoliarPhoto photo) { return "/api/v1/records/" + photo.getRecord().getId() + "/photos/" + photo.getId(); }

    private byte[] png(int color) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, color);
        try (var output = new ByteArrayOutputStream()) { ImageIO.write(image, "PNG", output); return output.toByteArray(); }
        finally { image.flush(); }
    }
}
