package com.example.wardrobe.garment.dto;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;

import java.math.BigDecimal;
import java.time.Instant;

public record GarmentSummaryResponse(
        Long id,
        String name,
        String size,
        Category category,
        Subcategory subcategory,
        Color color,
        String brand,
        GarmentCondition condition,
        GarmentStatus status,
        Season season,
        BigDecimal salePrice,
        String coverImageId,
        Instant createdAt,
        Instant updatedAt
) {
    public static GarmentSummaryResponse from(Garment g) {
        String coverImageId = null;
        if (g.getImages() != null && !g.getImages().isEmpty()) {
            coverImageId = String.valueOf(g.getImages().get(0).getId());
        }
        return new GarmentSummaryResponse(
                g.getId(),
                g.getName(),
                g.getSize(),
                g.getCategory(),
                g.getSubcategory(),
                g.getColor(),
                g.getBrand(),
                g.getCondition(),
                g.getStatus(),
                g.getSeason(),
                g.getSalePrice(),
                coverImageId,
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }
}
