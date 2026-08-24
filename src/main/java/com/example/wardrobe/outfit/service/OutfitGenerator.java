package com.example.wardrobe.outfit.service;

import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.outfit.entity.SavedOutfitItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OutfitGenerator {

    private final GarmentRepository garmentRepository;

    public OutfitGenerator(GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedOutfitItem> generate(Long ownerId, Season season,
                                          boolean includeOuterwear,
                                          boolean includeAccessories) {
        List<Garment> pool = garmentRepository.findByOwnerIdAndSeasonAndStatusNot(
                ownerId, season, GarmentStatus.SOLD);
        if (pool.isEmpty()) {
            throw new ConflictException("INSUFFICIENT_GARMENTS",
                    "You don't have any non-sold garments for season " + season);
        }

        Map<Category, List<Garment>> byCategory = bucketByCategory(pool);
        List<Garment> dresses = byCategory.getOrDefault(Category.DRESS, List.of());
        List<Garment> tops = byCategory.getOrDefault(Category.TOP, List.of());
        List<Garment> bottoms = byCategory.getOrDefault(Category.BOTTOM, List.of());
        List<Garment> shoes = byCategory.getOrDefault(Category.SHOES, List.of());
        List<Garment> outerwear = byCategory.getOrDefault(Category.OUTERWEAR, List.of());
        List<Garment> accessories = byCategory.getOrDefault(Category.ACCESSORIES, List.of());

        Random rng = ThreadLocalRandom.current();
        List<Garment> picked = new ArrayList<>();

        // Decide: dress OR top+bottom.
        boolean wantDress = !dresses.isEmpty()
                && (tops.isEmpty() || bottoms.isEmpty() || rng.nextBoolean());
        if (wantDress) {
            picked.add(pickOne(dresses, rng));
        } else if (!tops.isEmpty() && !bottoms.isEmpty()) {
            picked.add(pickOne(tops, rng));
            picked.add(pickOne(bottoms, rng));
        } else {
            throw new ConflictException("INSUFFICIENT_GARMENTS",
                    "Need either a dress or both a top and a bottom for season " + season);
        }

        // Shoes are mandatory.
        if (shoes.isEmpty()) {
            throw new ConflictException("INSUFFICIENT_GARMENTS",
                    "You need at least one pair of shoes for season " + season);
        }
        picked.add(pickOne(shoes, rng));

        // Optional outerwear.
        if (includeOuterwear && !outerwear.isEmpty()) {
            picked.add(pickOne(outerwear, rng));
        }

        // Optional accessories (0-2, distinct garments).
        if (includeAccessories && !accessories.isEmpty()) {
            int max = Math.min(2, accessories.size());
            int n = 1 + rng.nextInt(max); // 1..max
            Collections.shuffle(accessories, rng);
            for (int i = 0; i < n; i++) {
                picked.add(accessories.get(i));
            }
        }

        // Wrap in SavedOutfitItem placeholders. They will be attached to a
        // SavedOutfit only if the caller persists; the random endpoint returns
        // them transient, so we use a small transient wrapper here.
        List<SavedOutfitItem> items = new ArrayList<>();
        int order = 0;
        for (Garment g : picked) {
            items.add(toTransientItem(g, order++));
        }
        return items;
    }

    private static Map<Category, List<Garment>> bucketByCategory(List<Garment> garments) {
        Map<Category, List<Garment>> map = new EnumMap<>(Category.class);
        for (Garment g : garments) {
            map.computeIfAbsent(g.getCategory(), k -> new ArrayList<>()).add(g);
        }
        return map;
    }

    private static Garment pickOne(List<Garment> bucket, Random rng) {
        return bucket.get(rng.nextInt(bucket.size()));
    }

    private static SavedOutfitItem toTransientItem(Garment g, int order) {
        SavedOutfitItem item = new SavedOutfitItem();
        item.setGarment(g);
        item.setCategory(g.getCategory());
        item.setSnapshotName(g.getName());
        if (g.getImages() != null && !g.getImages().isEmpty()) {
            item.setSnapshotCoverImageId(String.valueOf(g.getImages().get(0).getId()));
        }
        item.setSortOrder(order);
        return item;
    }
}
