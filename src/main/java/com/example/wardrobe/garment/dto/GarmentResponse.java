package com.example.wardrobe.garment.dto;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import com.example.wardrobe.image.dto.ImageResponse;
import com.example.wardrobe.location.entity.Location;

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
        Subcategory subcategory,
        Color color,
        String brand,
        GarmentCondition condition,
        GarmentStatus status,
        Season season,
        BigDecimal salePrice,
        BigDecimal purchasePrice,
        String notes,
        Long locationId,
        String locationName,
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
        Location loc = g.getLocation();
        return new GarmentResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.getSize(),
                g.getCategory(),
                g.getSubcategory(),
                g.getColor(),
                g.getBrand(),
                g.getCondition(),
                g.getStatus(),
                g.getSeason(),
                g.getSalePrice(),
                g.getPurchasePrice(),
                g.getNotes(),
                loc == null ? null : loc.getId(),
                loc == null ? null : loc.getName(),
                g.getSoldAt(),
                g.getCreatedAt(),
                g.getUpdatedAt(),
                images
        );
    }
}
