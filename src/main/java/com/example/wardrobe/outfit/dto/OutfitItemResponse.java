package com.example.wardrobe.outfit.dto;

import com.example.wardrobe.garment.dto.GarmentSummaryResponse;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.outfit.entity.SavedOutfitItem;

import java.util.List;

/**
 * A single garment in an outfit, paired with its category role in the outfit.
 * If the garment is still present in the wardrobe we include its live summary;
 * otherwise we fall back to the snapshot fields stored on SavedOutfitItem.
 */
public record OutfitItemResponse(
        Long garmentId,
        Category category,
        String name,
        String coverImageId,
        GarmentSummaryResponse garment
) {
    public static OutfitItemResponse from(SavedOutfitItem item) {
        Garment g = item.getGarment();
        boolean alive = g != null && g.getId() != null;
        GarmentSummaryResponse summary = alive ? GarmentSummaryResponse.from(g) : null;
        return new OutfitItemResponse(
                alive ? g.getId() : null,
                item.getCategory(),
                alive ? g.getName() : item.getSnapshotName(),
                alive
                        ? (g.getImages() != null && !g.getImages().isEmpty()
                            ? String.valueOf(g.getImages().get(0).getId())
                            : null)
                        : item.getSnapshotCoverImageId(),
                summary
        );
    }

    public static List<OutfitItemResponse> fromAll(List<SavedOutfitItem> items) {
        return items.stream().map(OutfitItemResponse::from).toList();
    }
}
