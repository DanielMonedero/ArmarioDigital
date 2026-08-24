package com.example.wardrobe.garment.repository;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class GarmentSpecifications {

    private GarmentSpecifications() {
    }

    public static Specification<Garment> belongsToOwner(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Garment> hasStatus(GarmentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Garment> hasCategory(Category category) {
        return (root, query, cb) -> category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }

    public static Specification<Garment> hasSize(String size) {
        return (root, query, cb) -> (size == null || size.isBlank())
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("size")), size.toLowerCase());
    }

    public static Specification<Garment> hasBrand(String brand) {
        return (root, query, cb) -> (brand == null || brand.isBlank())
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    public static Specification<Garment> hasColor(String color) {
        return (root, query, cb) -> (color == null || color.isBlank())
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("color")), color.toLowerCase());
    }

    public static Specification<Garment> hasCondition(GarmentCondition condition) {
        return (root, query, cb) -> condition == null ? cb.conjunction() : cb.equal(root.get("condition"), condition);
    }

    public static Specification<Garment> textSearch(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            predicates.add(cb.like(cb.lower(root.get("brand")), pattern));
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
