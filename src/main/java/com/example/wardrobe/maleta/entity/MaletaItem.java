package com.example.wardrobe.maleta.entity;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.garment.entity.Garment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * One garment in a user's "Maleta" (travel suitcase). Pure reference
 * layer: storing a MaletaItem does not move the underlying garment
 * out of the wardrobe and does not change its status. ON DELETE
 * CASCADE on both FKs means deleting a user or a garment removes
 * the corresponding maleta rows automatically.
 */
@Entity
@Table(
    name = "maleta_items",
    uniqueConstraints = @UniqueConstraint(name = "uq_maleta_owner_garment", columnNames = {"owner_id", "garment_id"})
)
@EntityListeners(AuditingEntityListener.class)
public class MaletaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garment_id", nullable = false)
    private Garment garment;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public MaletaItem() {
    }

    public MaletaItem(User owner, Garment garment) {
        this.owner = owner;
        this.garment = garment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Garment getGarment() { return garment; }
    public void setGarment(Garment garment) { this.garment = garment; }

    public Instant getAddedAt() { return addedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaletaItem that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}