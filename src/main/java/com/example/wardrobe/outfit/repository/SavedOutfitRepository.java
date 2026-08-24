package com.example.wardrobe.outfit.repository;

import com.example.wardrobe.outfit.entity.SavedOutfit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedOutfitRepository extends JpaRepository<SavedOutfit, Long> {

    List<SavedOutfit> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<SavedOutfit> findByIdAndOwnerId(Long id, Long ownerId);
}
