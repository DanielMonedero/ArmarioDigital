package com.example.wardrobe.garment.entity;

import java.util.Arrays;
import java.util.List;

public enum Subcategory {
    // TOP
    T_SHIRT(Category.TOP),
    SHIRT(Category.TOP),
    POLO(Category.TOP),
    TANK_TOP(Category.TOP),
    BLOUSE(Category.TOP),

    // SWEATER
    SWEATER(Category.SWEATER),
    HOODIE(Category.SWEATER),
    CARDIGAN(Category.SWEATER),

    // OUTERWEAR
    JACKET(Category.OUTERWEAR),
    COAT(Category.OUTERWEAR),
    BLAZER(Category.OUTERWEAR),
    VEST(Category.OUTERWEAR),

    // BOTTOM
    JEANS(Category.BOTTOM),
    CHINOS(Category.BOTTOM),
    DRESS_PANTS(Category.BOTTOM),
    JOGGERS(Category.BOTTOM),
    LINEN_PANTS(Category.BOTTOM),
    SHORTS(Category.BOTTOM),
    LEGGINGS(Category.BOTTOM),

    // SKIRT
    MINI_SKIRT(Category.SKIRT),
    MIDI_SKIRT(Category.SKIRT),
    MAXI_SKIRT(Category.SKIRT),

    // DRESS
    SHORT_DRESS(Category.DRESS),
    LONG_DRESS(Category.DRESS),

    // SHOES
    SNEAKERS(Category.SHOES),
    BOOTS(Category.SHOES),
    SANDALS(Category.SHOES),
    HEELED(Category.SHOES),
    FLATS(Category.SHOES),

    // ACCESSORIES
    BELT(Category.ACCESSORIES),
    BAG(Category.ACCESSORIES),
    HAT(Category.ACCESSORIES),
    SCARF(Category.ACCESSORIES),

    // OTHER
    OTHER(Category.OTHER);

    private final Category parent;

    Subcategory(Category parent) {
        this.parent = parent;
    }

    public Category getParent() {
        return parent;
    }

    public static List<Subcategory> forCategory(Category category) {
        if (category == null) {
            return List.of();
        }
        return Arrays.stream(values())
                .filter(s -> s.parent == category)
                .toList();
    }
}
