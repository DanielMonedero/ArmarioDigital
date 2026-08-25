package com.example.wardrobe.garment.repository;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GarmentRepository extends JpaRepository<Garment, Long>, JpaSpecificationExecutor<Garment> {

    Optional<Garment> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndLocationId(Long ownerId, Long locationId);

    List<Garment> findByOwnerIdAndSeasonAndStatusNot(Long ownerId, Season season, GarmentStatus excludedStatus);

    List<Garment> findByOwnerIdAndIdIn(Long ownerId, List<Long> ids);

    /**
     * Returns rows of (Category, Subcategory-or-null, count) for every
     * garment owned by `ownerId` that is currently in the wardrobe
     * (i.e. status = WARDROBE). Garments without a subcategory appear
     * with subcategory = null in the second column.
     */
    @Query("""
            SELECT g.category, g.subcategory, COUNT(g)
            FROM Garment g
            WHERE g.owner.id = :ownerId AND g.status = com.example.wardrobe.garment.entity.GarmentStatus.WARDROBE
            GROUP BY g.category, g.subcategory
            """)
    List<Object[]> countWardrobeByCategoryAndSubcategory(@Param("ownerId") Long ownerId);
}
