package com.example.wardrobe.outfit.dto;

import com.example.wardrobe.garment.entity.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavedOutfitCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must be at most 160 characters")
        String name,

        @NotNull(message = "Season is required")
        Season season,

        boolean includeOuterwear,
        boolean includeAccessories,

        @NotEmpty(message = "Outfit must contain at least one garment")
        List<@NotNull @Positive Long> garmentIds
) {
}
