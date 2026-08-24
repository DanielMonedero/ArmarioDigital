package com.example.wardrobe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 1, max = 64, message = "Username must be 1-64 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 1, max = 200, message = "Password is required")
        String password
) {
}
