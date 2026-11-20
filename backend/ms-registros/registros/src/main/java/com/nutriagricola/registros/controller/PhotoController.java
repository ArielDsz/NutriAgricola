package com.nutriagricola.registros.controller;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.dto.PhotoResponse;
import com.nutriagricola.registros.service.PhotoService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/records/{recordId}/photos/{photoId}")
public class PhotoController {
    private final PhotoService photos;

    public PhotoController(PhotoService photos) {
        this.photos = photos;
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhotoResponse upload(@PathVariable String recordId, @PathVariable String photoId,
                                 @RequestPart("file") MultipartFile file,
                                 @AuthenticationPrincipal UserClientResponse user) {
        return photos.upload(recordId, photoId, file, user);
    }

    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable String recordId, @PathVariable String photoId,
                                           @AuthenticationPrincipal UserClientResponse user) {
        var photo = photos.download(recordId, photoId, user);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.noStore()).body(photo.content());
    }
}
