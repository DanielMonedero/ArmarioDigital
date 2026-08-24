package com.example.wardrobe.outfit.dto;

import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.outfit.entity.SavedOutfit;

import java.time.Instant;
import java.util.List;

public record SavedOutfitResponse(
        Long id,
        String name,
        Season season,
        boolean includeOuterwear,
        boolean includeAccessories,
        Instant createdAt,
        List<OutfitItemResponse> items
) {
    public static SavedOutfitResponse from(SavedOutfit o) {
        return new SavedOutfitResponse(
                o.getId(),
                o.getName(),
                o.getSeason(),
                o.isIncludeOuterwear(),
                o.isIncludeAccessories(),
                o.getCreatedAt(),
                OutfitItemResponse.fromAll(o.getItems())
        );
    }
}
