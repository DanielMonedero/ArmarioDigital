package com.example.wardrobe.auth.service;

import com.example.wardrobe.auth.dto.CreateUserRequest;
import com.example.wardrobe.auth.dto.UserResponse;
import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("USERNAME_TAKEN", "Username is already taken");
        }
        String hash = passwordEncoder.encode(request.password());
        String displayName = request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName().trim()
                : username;
        User user = new User(username, hash, displayName);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.from(user);
    }
}
