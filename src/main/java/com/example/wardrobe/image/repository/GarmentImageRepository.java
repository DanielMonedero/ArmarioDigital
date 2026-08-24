package com.example.wardrobe.image.repository;

import com.example.wardrobe.image.entity.GarmentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GarmentImageRepository extends JpaRepository<GarmentImage, Long> {

    List<GarmentImage> findByGarmentIdOrderBySortOrderAscIdAsc(Long garmentId);

    long countByGarmentId(Long garmentId);

    Optional<GarmentImage> findByIdAndGarmentId(Long id, Long garmentId);

    List<GarmentImage> findByIdInAndGarmentId(List<Long> ids, Long garmentId);
}
