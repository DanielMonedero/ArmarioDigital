package com.example.wardrobe.location.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.location.dto.LocationRequest;
import com.example.wardrobe.location.dto.LocationResponse;
import com.example.wardrobe.location.entity.Location;
import com.example.wardrobe.location.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository,
                           GarmentRepository garmentRepository,
                           UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.garmentRepository = garmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> list(Long ownerId) {
        return locationRepository.findByOwnerIdOrderByNameAsc(ownerId)
                .stream()
                .map(loc -> LocationResponse.from(loc, countGarments(loc.getId(), ownerId)))
                .toList();
    }

    @Transactional
    public LocationResponse create(Long ownerId, LocationRequest request) {
        String normalized = normalizeName(request.name());
        locationRepository.findByOwnerIdAndNameIgnoreCase(ownerId, normalized).ifPresent(l -> {
            throw new ConflictException("LOCATION_NAME_TAKEN", "A location with this name already exists");
        });
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Location loc = new Location(owner, normalized);
        Location saved = locationRepository.save(loc);
        return LocationResponse.from(saved, 0);
    }

    @Transactional
    public LocationResponse update(Long ownerId, Long id, LocationRequest request) {
        Location loc = ownedOrThrow(ownerId, id);
        String normalized = normalizeName(request.name());
        if (!loc.getName().equalsIgnoreCase(normalized)) {
            locationRepository.findByOwnerIdAndNameIgnoreCase(ownerId, normalized).ifPresent(other -> {
                if (!other.getId().equals(loc.getId())) {
                    throw new ConflictException("LOCATION_NAME_TAKEN", "A location with this name already exists");
                }
            });
            loc.setName(normalized);
        }
        long count = countGarments(loc.getId(), ownerId);
        return LocationResponse.from(loc, count);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        Location loc = ownedOrThrow(ownerId, id);
        long count = countGarments(loc.getId(), ownerId);
        if (count > 0) {
            throw new ConflictException("LOCATION_IN_USE",
                    "Location is used by " + count + " garment(s); reassign or delete them first");
        }
        locationRepository.delete(loc);
    }

    /**
     * Idempotent: returns the existing location with the given name for the owner,
     * or creates a new one. Used by the garment endpoint when the client passes
     * a locationName without a locationId.
     */
    @Transactional
    public Location findOrCreate(Long ownerId, String rawName) {
        String normalized = normalizeName(rawName);
        return locationRepository.findByOwnerIdAndNameIgnoreCase(ownerId, normalized)
                .orElseGet(() -> {
                    User owner = userRepository.findById(ownerId)
                            .orElseThrow(() -> new NotFoundException("User not found"));
                    Location loc = new Location(owner, normalized);
                    return locationRepository.save(loc);
                });
    }

    private Location ownedOrThrow(Long ownerId, Long id) {
        return locationRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }

    private long countGarments(Long locationId, Long ownerId) {
        return garmentRepository.countByOwnerIdAndLocationId(ownerId, locationId);
    }

    private static String normalizeName(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Location name must not be blank");
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Location name must not be blank");
        }
        return t;
    }
}
