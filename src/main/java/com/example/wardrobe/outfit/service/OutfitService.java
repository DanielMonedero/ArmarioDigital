package com.example.wardrobe.outfit.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.outfit.dto.OutfitItemResponse;
import com.example.wardrobe.outfit.dto.SavedOutfitCreateRequest;
import com.example.wardrobe.outfit.dto.SavedOutfitResponse;
import com.example.wardrobe.outfit.entity.SavedOutfit;
import com.example.wardrobe.outfit.entity.SavedOutfitItem;
import com.example.wardrobe.outfit.repository.SavedOutfitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OutfitService {

    private final SavedOutfitRepository savedOutfitRepository;
    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;
    private final OutfitGenerator outfitGenerator;

    public OutfitService(SavedOutfitRepository savedOutfitRepository,
                         GarmentRepository garmentRepository,
                         UserRepository userRepository,
                         OutfitGenerator outfitGenerator) {
        this.savedOutfitRepository = savedOutfitRepository;
        this.garmentRepository = garmentRepository;
        this.userRepository = userRepository;
        this.outfitGenerator = outfitGenerator;
    }

    @Transactional(readOnly = true)
    public List<SavedOutfitResponse> list(Long ownerId) {
        return savedOutfitRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(SavedOutfitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SavedOutfitResponse getById(Long ownerId, Long id) {
        SavedOutfit outfit = ownedOrThrow(ownerId, id);
        return SavedOutfitResponse.from(outfit);
    }

    @Transactional(readOnly = true)
    public List<OutfitItemResponse> generate(Long ownerId, Season season,
                                             boolean includeOuterwear,
                                             boolean includeAccessories) {
        List<SavedOutfitItem> items = outfitGenerator.generate(ownerId, season, includeOuterwear, includeAccessories);
        return OutfitItemResponse.fromAll(items);
    }

    @Transactional
    public SavedOutfitResponse save(Long ownerId, SavedOutfitCreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Garment> garments = garmentRepository.findByOwnerIdAndIdIn(ownerId, request.garmentIds());
        Map<Long, Garment> byId = garments.stream().collect(Collectors.toMap(Garment::getId, g -> g));
        Set<Long> missing = new HashSet<>();
        for (Long id : request.garmentIds()) {
            if (!byId.containsKey(id)) missing.add(id);
        }
        if (!missing.isEmpty()) {
            throw new BadRequestException("UNKNOWN_GARMENTS",
                    "These garments don't belong to you or don't exist: " + missing);
        }

        // Verify all garments match the requested season.
        for (Garment g : garments) {
            if (g.getSeason() != request.season()) {
                throw new BadRequestException("SEASON_MISMATCH",
                        "Garment " + g.getId() + " (" + g.getName() + ") is " + g.getSeason()
                                + " but outfit is " + request.season());
            }
        }

        SavedOutfit outfit = new SavedOutfit();
        outfit.setOwner(owner);
        outfit.setName(request.name().trim());
        outfit.setSeason(request.season());
        outfit.setIncludeOuterwear(request.includeOuterwear());
        outfit.setIncludeAccessories(request.includeAccessories());

        int order = 0;
        for (Long id : request.garmentIds()) {
            Garment g = byId.get(id);
            SavedOutfitItem item = new SavedOutfitItem();
            item.setGarment(g);
            item.setCategory(g.getCategory());
            item.setSnapshotName(g.getName());
            if (g.getImages() != null && !g.getImages().isEmpty()) {
                item.setSnapshotCoverImageId(String.valueOf(g.getImages().get(0).getId()));
            }
            item.setSortOrder(order++);
            outfit.addItem(item);
        }

        SavedOutfit saved = savedOutfitRepository.save(outfit);
        return SavedOutfitResponse.from(saved);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        SavedOutfit outfit = ownedOrThrow(ownerId, id);
        savedOutfitRepository.delete(outfit);
    }

    private SavedOutfit ownedOrThrow(Long ownerId, Long id) {
        return savedOutfitRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Outfit not found"));
    }
}
