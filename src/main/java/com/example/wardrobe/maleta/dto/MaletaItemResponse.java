package com.example.wardrobe.maleta.dto;

import com.example.wardrobe.garment.dto.GarmentSummaryResponse;
import com.example.wardrobe.maleta.entity.MaletaItem;

import java.time.Instant;

/**
 * A garment inside the user's "Maleta" plus the snapshot data the UI
 * needs to render the row (added-at timestamp and the live garment
 * summary so the card stays in sync with the wardrobe).
 */
public record MaletaItemResponse(
        Long id,
        Long garmentId,
        Instant addedAt,
        GarmentSummaryResponse garment
) {
    public static MaletaItemResponse from(MaletaItem item) {
        return new MaletaItemResponse(
                item.getId(),
                item.getGarment().getId(),
                item.getAddedAt(),
                GarmentSummaryResponse.from(item.getGarment())
        );
    }
}