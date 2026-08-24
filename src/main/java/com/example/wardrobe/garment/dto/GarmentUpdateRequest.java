package com.example.wardrobe.garment.dto;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GarmentUpdateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must be at most 160 characters")
        String name,

        @Size(max = 4000, message = "Description must be at most 4000 characters")
        String description,

        @NotBlank(message = "Size is required")
        @Size(max = 40, message = "Size must be at most 40 characters")
        String size,

        @NotNull(message = "Category is required")
        Category category,

        Subcategory subcategory,

        Color color,

        @Size(max = 120, message = "Brand must be at most 120 characters")
        String brand,

        @NotNull(message = "Condition is required")
        GarmentCondition condition,

        @NotNull(message = "Status is required")
        GarmentStatus status,

        @NotNull(message = "Season is required")
        Season season,

        @DecimalMin(value = "0.00", inclusive = true, message = "Sale price must be >= 0")
        BigDecimal salePrice,

        @DecimalMin(value = "0.00", inclusive = true, message = "Purchase price must be >= 0")
        BigDecimal purchasePrice,

        @Size(max = 4000, message = "Notes must be at most 4000 characters")
        String notes,

        @Positive
        Long locationId,

        @Size(max = 120, message = "Location name must be at most 120 characters")
        String locationName
) {
}
