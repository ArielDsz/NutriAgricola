package com.nutriagricola.registros.storage;

import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalPhotoStorageTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"JPEG", "PNG"})
    void storesReadsAndConfirmsIdenticalFilesWithoutOverwritingDifferentContent(String format) throws Exception {
        LocalPhotoStorage storage = new LocalPhotoStorage(directory.toString(), "10MB");
        String id = UUID.randomUUID().toString();
        byte[] bytes = image(format, 0);
        var upload = new MockMultipartFile("file", "nombre-no-confiable.txt", "application/octet-stream", bytes);

        var saved = storage.store(id, upload);
        assertEquals(id, saved.storageKey());
        assertEquals("JPEG".equals(format) ? "image/jpeg" : "image/png", saved.contentType());
        assertEquals(bytes.length, saved.sizeBytes());
        assertArrayEquals(bytes, storage.read(saved.storageKey()));
        assertEquals(saved, storage.store(id, upload));

        var changed = new MockMultipartFile("file", "otra.jpg", "image/jpeg", image(format, 0xFFFFFF));
        assertThrows(ResourceConflictException.class, () -> storage.store(id, changed));
        String otherFormat = "JPEG".equals(format) ? "PNG" : "JPEG";
        assertThrows(ResourceConflictException.class, () -> storage.store(id,
                new MockMultipartFile("file", image(otherFormat, 0))));
        assertArrayEquals(bytes, storage.read(saved.storageKey()));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void rejectsEmptyUnsupportedAndCorruptImages() throws Exception {
        LocalPhotoStorage storage = new LocalPhotoStorage(directory.toString(), "10MB");
        String id = UUID.randomUUID().toString();
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> storage.store(id,
                new MockMultipartFile("file", new byte[0]))).getStatusCode().value());
        assertEquals(415, assertThrows(ResponseStatusException.class, () -> storage.store(id,
                new MockMultipartFile("file", "falsa.jpg", "image/jpeg", new byte[]{1, 2, 3})))
                .getStatusCode().value());
        assertEquals(415, assertThrows(ResponseStatusException.class, () -> storage.store(id,
                new MockMultipartFile("file", image("GIF", 0)))).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> storage.store(id,
                new MockMultipartFile("file", Arrays.copyOf(image("PNG", 0), 24)))).getStatusCode().value());
        try (var files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void rejectsFilesAboveConfiguredSize() throws Exception {
        LocalPhotoStorage storage = new LocalPhotoStorage(directory.toString(), "10B");
        assertEquals(413, assertThrows(ResponseStatusException.class, () -> storage.store(UUID.randomUUID().toString(),
                new MockMultipartFile("file", image("PNG", 0)))).getStatusCode().value());
    }

    @Test
    void rejectsPathsAndReportsMissingFiles() throws Exception {
        LocalPhotoStorage storage = new LocalPhotoStorage(directory.toString(), "10MB");
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> storage.read("../ajeno.png")).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> storage.store("../ajeno",
                new MockMultipartFile("file", image("PNG", 0)))).getStatusCode().value());
        assertThrows(ResourceNotFoundException.class, () -> storage.read(UUID.randomUUID().toString()));
    }

    private byte[] image(String format, int rgb) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                image.setRGB(x, y, rgb);
            }
        }
        try (var output = new ByteArrayOutputStream()) {
            assertTrue(ImageIO.write(image, format, output));
            return output.toByteArray();
        } finally {
            image.flush();
        }
    }
}
