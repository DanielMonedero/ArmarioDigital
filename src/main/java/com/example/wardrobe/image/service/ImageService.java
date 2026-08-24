package com.example.wardrobe.image.service;

import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.image.dto.ImageOrderRequest;
import com.example.wardrobe.image.dto.ImageResponse;
import com.example.wardrobe.image.entity.GarmentImage;
import com.example.wardrobe.image.repository.GarmentImageRepository;
import com.example.wardrobe.image.storage.FileSystemStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    private final FileSystemStorage fileSystemStorage;

    public ImageService(GarmentRepository garmentRepository,
                        GarmentImageRepository garmentImageRepository,
                        FileSystemStorage fileSystemStorage) {
        this.garmentRepository = garmentRepository;
        this.garmentImageRepository = garmentImageRepository;
        this.fileSystemStorage = fileSystemStorage;
    }

    @Transactional
    public ImageResponse upload(Long ownerId, Long garmentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("EMPTY_FILE", "File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("UNSUPPORTED_CONTENT_TYPE",
                    "Only JPEG, PNG and WebP images are allowed");
        }

        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        FileSystemStorage.StoredFile stored = fileSystemStorage.store(file);

        GarmentImage image = new GarmentImage();
        image.setGarment(garment);
        image.setFilename(stored.filename());
        image.setOriginalFilename(stored.originalFilename());
        image.setContentType(contentType);
        image.setSize(file.getSize());
        int nextOrder = garment.getImages().size();
        image.setSortOrder(nextOrder);
        garment.getImages().add(image);
        GarmentImage saved = garmentImageRepository.save(image);
        return ImageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StoredImageResource load(Long ownerId, Long garmentId, Long imageId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
        GarmentImage image = garmentImageRepository.findByIdAndGarmentId(imageId, garmentId)
                .orElseThrow(() -> new NotFoundException("Image not found"));
        if (!image.getGarment().getId().equals(garment.getId())) {
            throw new NotFoundException("Image not found");
        }
        Path path = fileSystemStorage.resolve(image.getFilename());
        if (!Files.exists(path)) {
            throw new NotFoundException("Image file missing on disk");
        }
        return new StoredImageResource(path, image.getContentType());
    }

    @Transactional
    public void delete(Long ownerId, Long garmentId, Long imageId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        GarmentImage image = garmentImageRepository.findByIdAndGarmentId(imageId, garmentId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        long count = garmentImageRepository.countByGarmentId(garmentId);
        if (count <= 1) {
            throw new ConflictException("LAST_IMAGE",
                    "A garment must always have at least one image");
        }

        garment.getImages().remove(image);
        garmentImageRepository.delete(image);
        fileSystemStorage.delete(image.getFilename());

        reindexSortOrder(garment.getId());
    }

    @Transactional
    public List<ImageResponse> reorder(Long ownerId, Long garmentId, ImageOrderRequest request) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        List<Long> requestedIds = request.imageIds();
        Set<Long> unique = new HashSet<>(requestedIds);
        if (unique.size() != requestedIds.size()) {
            throw new BadRequestException("DUPLICATE_IDS", "Image IDs must be unique");
        }

        List<GarmentImage> existing = garment.getImages();
        if (requestedIds.size() != existing.size()) {
            throw new BadRequestException("INCOMPLETE_LIST",
                    "Order list must include every image of the garment exactly once");
        }

        Set<Long> existingIds = new HashSet<>();
        for (GarmentImage img : existing) {
            existingIds.add(img.getId());
        }
        for (Long id : requestedIds) {
            if (!existingIds.contains(id)) {
                throw new BadRequestException("INVALID_IMAGE_ID", "Image does not belong to garment: " + id);
            }
        }

        for (int i = 0; i < requestedIds.size(); i++) {
            Long id = requestedIds.get(i);
            for (GarmentImage img : existing) {
                if (img.getId().equals(id)) {
                    img.setSortOrder(i);
                    break;
                }
            }
        }

        return existing.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(ImageResponse::from)
                .toList();
    }

    private void reindexSortOrder(Long garmentId) {
        List<GarmentImage> all = garmentImageRepository
                .findByGarmentIdOrderBySortOrderAscIdAsc(garmentId);
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setSortOrder(i);
        }
    }

    public record StoredImageResource(Path path, String contentType) {
    }
}
