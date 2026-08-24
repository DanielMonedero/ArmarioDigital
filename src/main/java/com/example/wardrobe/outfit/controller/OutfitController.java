package com.example.wardrobe.outfit.controller;

import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.outfit.dto.OutfitGenerateRequest;
import com.example.wardrobe.outfit.dto.OutfitItemResponse;
import com.example.wardrobe.outfit.dto.SavedOutfitCreateRequest;
import com.example.wardrobe.outfit.dto.SavedOutfitResponse;
import com.example.wardrobe.outfit.service.OutfitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    private final OutfitService outfitService;

    public OutfitController(OutfitService outfitService) {
        this.outfitService = outfitService;
    }

    @GetMapping
    public List<SavedOutfitResponse> list(Authentication authentication) {
        Long ownerId = currentUserId(authentication);
        return outfitService.list(ownerId);
    }

    @PostMapping("/generate")
    public List<OutfitItemResponse> generate(Authentication authentication,
                                              @Valid @RequestBody OutfitGenerateRequest request) {
        Long ownerId = currentUserId(authentication);
        return outfitService.generate(ownerId, request.season(),
                request.includeOuterwear(), request.includeAccessories());
    }

    @PostMapping
    public ResponseEntity<SavedOutfitResponse> save(Authentication authentication,
                                                     @Valid @RequestBody SavedOutfitCreateRequest request) {
        Long ownerId = currentUserId(authentication);
        SavedOutfitResponse response = outfitService.save(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public SavedOutfitResponse getById(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        return outfitService.getById(ownerId, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        outfitService.delete(ownerId, id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
