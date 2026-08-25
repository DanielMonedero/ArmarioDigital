package com.example.wardrobe.garment.controller;

import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.common.dto.PageResponse;
import com.example.wardrobe.garment.dto.GarmentCreateRequest;
import com.example.wardrobe.garment.dto.GarmentResponse;
import com.example.wardrobe.garment.dto.GarmentSummaryResponse;
import com.example.wardrobe.garment.dto.GarmentUpdateRequest;
import com.example.wardrobe.garment.dto.WardrobeStatsResponse;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import com.example.wardrobe.garment.service.GarmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/garments")
public class GarmentController {

    private final GarmentService garmentService;

    public GarmentController(GarmentService garmentService) {
        this.garmentService = garmentService;
    }

    @GetMapping
    public PageResponse<GarmentSummaryResponse> list(
            Authentication authentication,
            @RequestParam(required = false) GarmentStatus status,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Subcategory subcategory,
            @RequestParam(required = false) Color color,
            @RequestParam(required = false) Season season,
            @RequestParam(required = false) Long locationId,
            @RequestParam(name = "garmentSize", required = false) String garmentSize,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) GarmentCondition condition,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long ownerId = currentUserId(authentication);
        return garmentService.list(ownerId, status, category, subcategory, color, season, locationId,
                garmentSize, brand, condition, search, pageable);
    }

    @PostMapping
    public ResponseEntity<GarmentResponse> create(Authentication authentication,
                                                  @Valid @RequestBody GarmentCreateRequest request) {
        Long ownerId = currentUserId(authentication);
        GarmentResponse response = garmentService.create(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public GarmentResponse getById(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        return garmentService.getById(ownerId, id);
    }

    @GetMapping("/stats")
    public WardrobeStatsResponse wardrobeStats(Authentication authentication) {
        Long ownerId = currentUserId(authentication);
        return garmentService.wardrobeStats(ownerId);
    }

    @PutMapping("/{id}")
    public GarmentResponse update(Authentication authentication,
                                  @PathVariable Long id,
                                  @Valid @RequestBody GarmentUpdateRequest request) {
        Long ownerId = currentUserId(authentication);
        return garmentService.update(ownerId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        garmentService.delete(ownerId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/put-for-sale")
    public GarmentResponse putForSale(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        return garmentService.putForSale(ownerId, id);
    }

    @PostMapping("/{id}/move-to-wardrobe")
    public GarmentResponse moveToWardrobe(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        return garmentService.moveToWardrobe(ownerId, id);
    }

    @PostMapping("/{id}/mark-as-sold")
    public GarmentResponse markAsSold(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        return garmentService.markAsSold(ownerId, id);
    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
