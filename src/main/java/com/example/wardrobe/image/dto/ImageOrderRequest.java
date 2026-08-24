package com.example.wardrobe.image.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImageOrderRequest(
        @NotEmpty(message = "Image IDs must not be empty")
        List<Long> imageIds
) {
}
