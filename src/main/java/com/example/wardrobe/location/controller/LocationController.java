package com.example.wardrobe.location.controller;

import com.example.wardrobe.auth.service.CustomUserDetailsService;
import com.example.wardrobe.location.dto.LocationRequest;
import com.example.wardrobe.location.dto.LocationResponse;
import com.example.wardrobe.location.service.LocationService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<LocationResponse> list(Authentication authentication) {
        Long ownerId = currentUserId(authentication);
        return locationService.list(ownerId);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(Authentication authentication,
                                                   @Valid @RequestBody LocationRequest request) {
        Long ownerId = currentUserId(authentication);
        // The service needs the owner to enforce uniqueness; the create method only
        // validates the name and persists the entity; the owner is wired separately
        // via the repository's lookup. To keep the API thin we attach the owner
        // here by passing it through the service in a small wrapper call.
        LocationResponse response = locationService.create(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public LocationResponse update(Authentication authentication,
                                   @PathVariable Long id,
                                   @Valid @RequestBody LocationRequest request) {
        Long ownerId = currentUserId(authentication);
        return locationService.update(ownerId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long ownerId = currentUserId(authentication);
        locationService.delete(ownerId, id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        CustomUserDetailsService.AppUserPrincipal principal =
                (CustomUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
