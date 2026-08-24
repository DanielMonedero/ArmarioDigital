package com.example.wardrobe.image.dto;

import com.example.wardrobe.image.entity.GarmentImage;

import java.time.Instant;

public record ImageResponse(
        Long id,
        Long garmentId,
        String filename,
        String originalFilename,
        String contentType,
        long size,
        int sortOrder,
        Instant createdAt
) {
    public static ImageResponse from(GarmentImage image) {
        return new ImageResponse(
                image.getId(),
                image.getGarment() != null ? image.getGarment().getId() : null,
                image.getFilename(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSize(),
                image.getSortOrder(),
                image.getCreatedAt()
        );
    }
}
