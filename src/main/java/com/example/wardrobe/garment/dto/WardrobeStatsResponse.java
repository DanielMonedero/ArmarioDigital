package com.example.wardrobe.garment.dto;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Subcategory;

import java.util.List;

/**
 * Aggregate counts of the user's WARDROBE (only status = WARDROBE,
 * never FOR_SALE / SOLD). The shape is intentionally redundant:
 * `total` for the headline number, `byCategory` for the primary bar
 * chart, and `details` for the full (category, subcategory) breakdown.
 */
public record WardrobeStatsResponse(
        long total,
        List<CategoryBucket> byCategory,
        List<Detail> details
) {
    public record CategoryBucket(
            Category category,
            long count,
            List<SubcategoryBucket> subcategories
    ) {}

    public record SubcategoryBucket(
            Subcategory subcategory,
            long count
    ) {}

    public record Detail(
            Category category,
            Subcategory subcategory,
            long count
    ) {}
}