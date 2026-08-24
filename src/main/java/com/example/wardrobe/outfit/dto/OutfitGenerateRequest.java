package com.example.wardrobe.outfit.dto;

import com.example.wardrobe.garment.entity.Season;
import jakarta.validation.constraints.NotNull;

public record OutfitGenerateRequest(
        @NotNull(message = "Season is required")
        Season season,

        boolean includeOuterwear,
        boolean includeAccessories
) {
}
