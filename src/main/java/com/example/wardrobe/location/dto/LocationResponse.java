package com.example.wardrobe.location.dto;

import com.example.wardrobe.location.entity.Location;

import java.time.Instant;

public record LocationResponse(
        Long id,
        String name,
        long garmentCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static LocationResponse from(Location l, long garmentCount) {
        return new LocationResponse(
                l.getId(),
                l.getName(),
                garmentCount,
                l.getCreatedAt(),
                l.getUpdatedAt()
        );
    }
}
