package com.example.wardrobe.maleta.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.maleta.dto.MaletaItemResponse;
import com.example.wardrobe.maleta.entity.MaletaItem;
import com.example.wardrobe.maleta.repository.MaletaItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaletaService {

    private final MaletaItemRepository maletaItemRepository;
    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;

    public MaletaService(MaletaItemRepository maletaItemRepository,
                         GarmentRepository garmentRepository,
                         UserRepository userRepository) {
        this.maletaItemRepository = maletaItemRepository;
        this.garmentRepository = garmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MaletaItemResponse> list(Long ownerId) {
        return maletaItemRepository.findByOwnerIdOrderByAddedAtDesc(ownerId)
                .stream()
                .map(MaletaItemResponse::from)
                .toList();
    }

    @Transactional
    public MaletaItemResponse add(Long ownerId, Long garmentId) {
        if (maletaItemRepository.existsByOwnerIdAndGarmentId(ownerId, garmentId)) {
            throw new ConflictException("MALETA_ALREADY_ADDED",
                    "La prenda ya está en la maleta");
        }
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
        if (garment.getStatus() != GarmentStatus.WARDROBE) {
            throw new BadRequestException("MALETA_NOT_AVAILABLE",
                    "Solo se pueden añadir prendas que estén en el Armario");
        }
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        MaletaItem saved = maletaItemRepository.save(new MaletaItem(owner, garment));
        return MaletaItemResponse.from(saved);
    }

    @Transactional
    public void remove(Long ownerId, Long garmentId) {
        MaletaItem item = maletaItemRepository.findByOwnerIdAndGarmentId(ownerId, garmentId)
                .orElseThrow(() -> new NotFoundException("La prenda no está en la maleta"));
        maletaItemRepository.delete(item);
    }

    @Transactional
    public int clear(Long ownerId) {
        long before = maletaItemRepository.countByOwnerId(ownerId);
        maletaItemRepository.deleteByOwnerId(ownerId);
        return (int) before;
    }
}