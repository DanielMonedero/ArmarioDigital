package com.example.wardrobe.maleta.controller;

import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.maleta.dto.MaletaItemResponse;
import com.example.wardrobe.maleta.service.MaletaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maleta")
public class MaletaController {

    private final MaletaService maletaService;

    public MaletaController(MaletaService maletaService) {
        this.maletaService = maletaService;
    }

    @GetMapping
    public List<MaletaItemResponse> list(Authentication authentication) {
        Long ownerId = currentUserId(authentication);
        return maletaService.list(ownerId);
    }

    @PostMapping("/{garmentId}")
    public ResponseEntity<MaletaItemResponse> add(Authentication authentication,
                                                   @PathVariable Long garmentId) {
        Long ownerId = currentUserId(authentication);
        MaletaItemResponse response = maletaService.add(ownerId, garmentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{garmentId}")
    public ResponseEntity<Void> remove(Authentication authentication,
                                       @PathVariable Long garmentId) {
        Long ownerId = currentUserId(authentication);
        maletaService.remove(ownerId, garmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication authentication) {
        Long ownerId = currentUserId(authentication);
        maletaService.clear(ownerId);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}