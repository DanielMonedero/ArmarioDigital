package com.example.wardrobe.auth.dto;

import com.example.wardrobe.auth.entity.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getCreatedAt()
        );
    }
}
