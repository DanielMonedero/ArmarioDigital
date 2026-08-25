package com.example.wardrobe.maleta.repository;

import com.example.wardrobe.maleta.entity.MaletaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaletaItemRepository extends JpaRepository<MaletaItem, Long> {

    List<MaletaItem> findByOwnerIdOrderByAddedAtDesc(Long ownerId);

    Optional<MaletaItem> findByOwnerIdAndGarmentId(Long ownerId, Long garmentId);

    boolean existsByOwnerIdAndGarmentId(Long ownerId, Long garmentId);

    long countByOwnerId(Long ownerId);

    void deleteByOwnerId(Long ownerId);
}