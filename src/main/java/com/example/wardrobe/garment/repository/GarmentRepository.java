package com.example.wardrobe.garment.repository;

import com.example.wardrobe.garment.entity.Garment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, Long>, JpaSpecificationExecutor<Garment> {

    Optional<Garment> findByIdAndOwnerId(Long id, Long ownerId);
}
