package com.example.wardrobe.location.dto;

import com.example.wardrobe.location.entity.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name
) {
    public static LocationRequest from(Location l) {
        return new LocationRequest(l.getName());
    }
}
