package com.example.wardrobe.garment.repository;

import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, Long>, JpaSpecificationExecutor<Garment> {

    Optional<Garment> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndLocationId(Long ownerId, Long locationId);

    List<Garment> findByOwnerIdAndSeasonAndStatusNot(Long ownerId, Season season, GarmentStatus excludedStatus);

    List<Garment> findByOwnerIdAndIdIn(Long ownerId, List<Long> ids);
}
