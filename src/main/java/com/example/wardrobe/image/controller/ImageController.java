package com.example.wardrobe.image.controller;

import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.image.dto.ImageOrderRequest;
import com.example.wardrobe.image.dto.ImageResponse;
import com.example.wardrobe.image.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/garments/{garmentId}/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<ImageResponse> upload(Authentication authentication,
                                                @PathVariable Long garmentId,
                                                @RequestParam("file") MultipartFile file) {
        Long ownerId = currentUserId(authentication);
        ImageResponse response = imageService.upload(ownerId, garmentId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> download(Authentication authentication,
                                             @PathVariable Long garmentId,
                                             @PathVariable Long imageId) throws IOException {
        Long ownerId = currentUserId(authentication);
        ImageService.StoredImageResource stored = imageService.load(ownerId, garmentId, imageId);
        Path path = stored.path();
        MediaType mediaType = resolveMediaType(stored.contentType(), path);
        Resource resource = new PathResource(path);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .body(resource);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(Authentication authentication,
                                       @PathVariable Long garmentId,
                                       @PathVariable Long imageId) {
        Long ownerId = currentUserId(authentication);
        imageService.delete(ownerId, garmentId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/order")
    public List<ImageResponse> reorder(Authentication authentication,
                                       @PathVariable Long garmentId,
                                       @Valid @RequestBody ImageOrderRequest request) {
        Long ownerId = currentUserId(authentication);
        return imageService.reorder(ownerId, garmentId, request);
    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }

    private MediaType resolveMediaType(String contentType, Path path) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            String name = path.getFileName().toString().toLowerCase();
            if (name.endsWith(".png")) return MediaType.IMAGE_PNG;
            if (name.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
            return MediaType.IMAGE_JPEG;
        }
    }
}
