package com.example.wardrobe.garment.dto;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.image.dto.ImageResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record GarmentResponse(
        Long id,
        String name,
        String description,
        String size,
        Category category,
        String color,
        String brand,
        GarmentCondition condition,
        GarmentStatus status,
        BigDecimal salePrice,
        BigDecimal purchasePrice,
        String notes,
        Instant soldAt,
        Instant createdAt,
        Instant updatedAt,
        List<ImageResponse> images
) {
    public static GarmentResponse from(Garment g) {
        List<ImageResponse> images = g.getImages() == null
                ? List.of()
                : g.getImages().stream()
                    .sorted(Comparator
                            .comparingInt(com.example.wardrobe.image.entity.GarmentImage::getSortOrder)
                            .thenComparing(com.example.wardrobe.image.entity.GarmentImage::getId))
                    .map(ImageResponse::from)
                    .toList();
        return new GarmentResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.getSize(),
                g.getCategory(),
                g.getColor(),
                g.getBrand(),
                g.getCondition(),
                g.getStatus(),
                g.getSalePrice(),
                g.getPurchasePrice(),
                g.getNotes(),
                g.getSoldAt(),
                g.getCreatedAt(),
                g.getUpdatedAt(),
                images
        );
    }
}
