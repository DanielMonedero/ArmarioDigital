package com.example.wardrobe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 64, message = "Username must be 3-64 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 200, message = "Password must be 8-200 characters")
        String password,

        @Size(max = 120, message = "Display name must be at most 120 characters")
        String displayName
) {
}
