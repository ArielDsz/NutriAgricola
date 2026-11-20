package com.nutriagricola.registros.storage;

import com.nutriagricola.registros.exception.ResourceConflictException;
import com.nutriagricola.registros.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalPhotoStorage {
    private static final long MAX_PIXELS = 25_000_000;
    private static final String KEY_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private final Path directory;
    private final long maxBytes;

    public LocalPhotoStorage(
            @Value("${storage.photos.directory:storage/foliar-photos}") String directory,
            @Value("${spring.servlet.multipart.max-file-size:10MB}") String maxFileSize) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        this.maxBytes = DataSize.parse(maxFileSize).toBytes();
        if (maxBytes < 1 || maxBytes >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El límite de fotografías no es válido");
        }
    }

    public StoredPhoto store(String photoId, MultipartFile file) {
        String canonicalId;
        try {
            canonicalId = UUID.fromString(photoId).toString();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de la fotografía no es válido");
        }
        if (!canonicalId.equals(photoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa el UUID completo en minúsculas");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fotografía está vacía");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La fotografía supera el tamaño permitido");
        }

        try {
            byte[] bytes;
            try (var input = file.getInputStream()) {
                bytes = input.readNBytes((int) maxBytes + 1);
            }
            if (bytes.length > maxBytes) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La fotografía supera el tamaño permitido");
            }
            String format = validateImage(bytes);
            String key = canonicalId;
            Path target = resolveKey(key);
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "upload-", ".tmp");
            try {
                Files.write(temporary, bytes);
                try {
                    Files.move(temporary, target);
                } catch (FileAlreadyExistsException ex) {
                    if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                            || !Arrays.equals(Files.readAllBytes(target), bytes)) {
                        throw new ResourceConflictException("La fotografía ya tiene un archivo diferente");
                    }
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
            return new StoredPhoto(key, "JPEG".equals(format) ? "image/jpeg" : "image/png", bytes.length);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la fotografía", ex);
        }
    }

    public byte[] read(String storageKey) {
        Path file = resolveKey(storageKey);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResourceNotFoundException("Archivo de fotografía no encontrado");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la fotografía", ex);
        }
    }

    private Path resolveKey(String key) {
        if (key == null || !key.matches(KEY_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave de la fotografía no es válida");
        }
        return directory.resolve(key);
    }

    private String validateImage(byte[] bytes) {
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Usa una imagen JPEG o PNG");
            }
            var reader = readers.next();
            try {
                String format = reader.getFormatName().toUpperCase(Locale.ROOT);
                if (!"JPEG".equals(format) && !"PNG".equals(format)) {
                    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Usa una imagen JPEG o PNG");
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > MAX_PIXELS) {
                    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La fotografía supera los 25 megapíxeles");
                }
                reader.read(0).flush();
                return format;
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no contiene una imagen legible");
        }
    }

    public record StoredPhoto(String storageKey, String contentType, long sizeBytes) { }
}
