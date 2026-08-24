package com.example.wardrobe.outfit.entity;

import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * One garment belonging to a saved outfit. We store both the FK to the garment
 * (for live detail navigation) and a snapshot of its display data (name + cover
 * image) so that if the underlying garment is later deleted, the outfit still
 * renders something sensible.
 */
@Entity
@Table(name = "saved_outfit_items")
public class SavedOutfitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outfit_id", nullable = false)
    private SavedOutfit outfit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garment_id", nullable = false)
    private Garment garment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Category category;

    @Column(nullable = false, length = 160)
    private String snapshotName;

    @Column(name = "snapshot_cover_image_id", length = 32)
    private String snapshotCoverImageId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public SavedOutfitItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SavedOutfit getOutfit() { return outfit; }
    public void setOutfit(SavedOutfit outfit) { this.outfit = outfit; }

    public Garment getGarment() { return garment; }
    public void setGarment(Garment garment) { this.garment = garment; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getSnapshotName() { return snapshotName; }
    public void setSnapshotName(String snapshotName) { this.snapshotName = snapshotName; }

    public String getSnapshotCoverImageId() { return snapshotCoverImageId; }
    public void setSnapshotCoverImageId(String snapshotCoverImageId) { this.snapshotCoverImageId = snapshotCoverImageId; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedOutfitItem that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
