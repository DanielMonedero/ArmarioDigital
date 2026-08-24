package com.example.wardrobe.location.repository;

import com.example.wardrobe.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByOwnerIdOrderByNameAsc(Long ownerId);

    Optional<Location> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Location> findByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    long countByOwnerId(Long ownerId);
}
