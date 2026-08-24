package com.example.wardrobe.outfit.entity;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.garment.entity.Season;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "saved_outfits")
@EntityListeners(AuditingEntityListener.class)
public class SavedOutfit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Season season;

    @Column(name = "include_outerwear", nullable = false)
    private boolean includeOuterwear;

    @Column(name = "include_accessories", nullable = false)
    private boolean includeAccessories;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private List<SavedOutfitItem> items = new ArrayList<>();

    public SavedOutfit() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public boolean isIncludeOuterwear() { return includeOuterwear; }
    public void setIncludeOuterwear(boolean includeOuterwear) { this.includeOuterwear = includeOuterwear; }

    public boolean isIncludeAccessories() { return includeAccessories; }
    public void setIncludeAccessories(boolean includeAccessories) { this.includeAccessories = includeAccessories; }

    public Instant getCreatedAt() { return createdAt; }

    public List<SavedOutfitItem> getItems() { return items; }
    public void setItems(List<SavedOutfitItem> items) { this.items = items; }

    public void addItem(SavedOutfitItem item) {
        items.add(item);
        item.setOutfit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedOutfit that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
