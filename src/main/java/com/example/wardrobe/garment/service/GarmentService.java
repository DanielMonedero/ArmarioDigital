package com.example.wardrobe.garment.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.dto.PageResponse;
import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.description.DescriptionGenerator;
import com.example.wardrobe.garment.dto.GarmentCreateRequest;
import com.example.wardrobe.garment.dto.GarmentResponse;
import com.example.wardrobe.garment.dto.GarmentSummaryResponse;
import com.example.wardrobe.garment.dto.GarmentUpdateRequest;
import com.example.wardrobe.garment.dto.WardrobeStatsResponse;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.garment.repository.GarmentSpecifications;
import com.example.wardrobe.image.entity.GarmentImage;
import com.example.wardrobe.image.repository.GarmentImageRepository;
import com.example.wardrobe.image.storage.FileSystemStorage;
import com.example.wardrobe.location.entity.Location;
import com.example.wardrobe.location.repository.LocationRepository;
import com.example.wardrobe.location.service.LocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class GarmentService {

    private final GarmentRepository garmentRepository;
    private final GarmentImageRepository garmentImageRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final DescriptionGenerator descriptionGenerator;
    private final FileSystemStorage fileSystemStorage;

    public GarmentService(GarmentRepository garmentRepository,
                          GarmentImageRepository garmentImageRepository,
                          UserRepository userRepository,
                          LocationRepository locationRepository,
                          LocationService locationService,
                          DescriptionGenerator descriptionGenerator,
                          FileSystemStorage fileSystemStorage) {
        this.garmentRepository = garmentRepository;
        this.garmentImageRepository = garmentImageRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.descriptionGenerator = descriptionGenerator;
        this.fileSystemStorage = fileSystemStorage;
    }

    @Transactional
    public GarmentResponse create(Long ownerId, GarmentCreateRequest request) {
        validateForCreate(request);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Garment garment = new Garment();
        garment.setOwner(owner);
        applyCreate(garment, request);
        Garment saved = garmentRepository.save(garment);
        return GarmentResponse.from(saved);
    }

    @Transactional
    public GarmentResponse update(Long ownerId, Long garmentId, GarmentUpdateRequest request) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
        validateForUpdate(garment.getStatus(), request);

        garment.setName(request.name().trim());
        garment.setDescription(request.description());
        garment.setSize(request.size().trim());
        garment.setCategory(request.category());
        garment.setSubcategory(request.subcategory());
        garment.setColor(request.color());
        garment.setBrand(emptyToNull(request.brand()));
        garment.setCondition(request.condition());
        garment.setStatus(request.status());
        garment.setSeason(request.season());
        garment.setSalePrice(request.salePrice());
        garment.setPurchasePrice(request.purchasePrice());
        garment.setNotes(request.notes());
        garment.setLocation(resolveLocation(ownerId, request.locationId(), request.locationName()));
        if (request.status() != GarmentStatus.SOLD) {
            garment.setSoldAt(null);
        }
        return GarmentResponse.from(garment);
    }

    @Transactional(readOnly = true)
    public GarmentResponse getById(Long ownerId, Long garmentId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
        return GarmentResponse.from(garment);
    }

    @Transactional(readOnly = true)
    public PageResponse<GarmentSummaryResponse> list(Long ownerId,
                                                      GarmentStatus status,
                                                      Category category,
                                                      Subcategory subcategory,
                                                      Color color,
                                                      Season season,
                                                      Long locationId,
                                                      String size,
                                                      String brand,
                                                      GarmentCondition condition,
                                                      String search,
                                                      Pageable pageable) {
        Specification<Garment> spec = Specification
                .where(GarmentSpecifications.belongsToOwner(ownerId))
                .and(GarmentSpecifications.hasStatus(status))
                .and(GarmentSpecifications.hasCategory(category))
                .and(GarmentSpecifications.hasSubcategory(subcategory))
                .and(GarmentSpecifications.hasColor(color))
                .and(GarmentSpecifications.hasSeason(season))
                .and(GarmentSpecifications.hasLocation(locationId))
                .and(GarmentSpecifications.hasSize(size))
                .and(GarmentSpecifications.hasBrand(brand))
                .and(GarmentSpecifications.hasCondition(condition))
                .and(GarmentSpecifications.textSearch(search));

        Page<Garment> page = garmentRepository.findAll(spec, pageable);
        return PageResponse.from(page, GarmentSummaryResponse::from);
    }

    @Transactional
    public void delete(Long ownerId, Long garmentId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
        for (GarmentImage image : garment.getImages()) {
            fileSystemStorage.delete(image.getFilename());
        }
        garmentRepository.delete(garment);
    }

    @Transactional
    public GarmentResponse putForSale(Long ownerId, Long garmentId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        if (garment.getStatus() == GarmentStatus.SOLD) {
            throw new ConflictException("INVALID_TRANSITION",
                    "Cannot put a sold garment back for sale directly; move it to wardrobe first");
        }
        if (garment.getImages() == null || garment.getImages().isEmpty()) {
            throw new ConflictException("MISSING_IMAGE",
                    "A garment must have at least one image before being put for sale");
        }
        if (garment.getSalePrice() == null) {
            throw new ConflictException("MISSING_PRICE",
                    "A sale price must be set before putting the garment for sale");
        }
        garment.setStatus(GarmentStatus.FOR_SALE);
        garment.setDescription(descriptionGenerator.generate(garment));
        return GarmentResponse.from(garment);
    }

    @Transactional
    public GarmentResponse moveToWardrobe(Long ownerId, Long garmentId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        if (garment.getStatus() == GarmentStatus.WARDROBE) {
            return GarmentResponse.from(garment);
        }
        garment.setStatus(GarmentStatus.WARDROBE);
        garment.setSoldAt(null);
        return GarmentResponse.from(garment);
    }

    @Transactional
    public GarmentResponse markAsSold(Long ownerId, Long garmentId) {
        Garment garment = garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));

        if (garment.getStatus() != GarmentStatus.FOR_SALE) {
            throw new ConflictException("INVALID_TRANSITION",
                    "Only garments currently for sale can be marked as sold");
        }
        garment.setStatus(GarmentStatus.SOLD);
        garment.setSoldAt(Instant.now());
        return GarmentResponse.from(garment);
    }

    public Garment requireOwnedGarment(Long ownerId, Long garmentId) {
        return garmentRepository.findByIdAndOwnerId(garmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Garment not found"));
    }

    private void applyCreate(Garment garment, GarmentCreateRequest request) {
        garment.setName(request.name().trim());
        garment.setDescription(request.description());
        garment.setSize(request.size().trim());
        garment.setCategory(request.category());
        garment.setSubcategory(request.subcategory());
        garment.setColor(request.color());
        garment.setBrand(emptyToNull(request.brand()));
        garment.setCondition(request.condition());
        garment.setStatus(request.status());
        garment.setSeason(request.season());
        garment.setSalePrice(request.salePrice());
        garment.setPurchasePrice(request.purchasePrice());
        garment.setNotes(request.notes());
        garment.setLocation(resolveLocation(garment.getOwner().getId(),
                request.locationId(), request.locationName()));
        if (request.status() == GarmentStatus.SOLD) {
            garment.setSoldAt(Instant.now());
        }
    }

    private Location resolveLocation(Long ownerId, Long locationId, String locationName) {
        if (locationId == null && (locationName == null || locationName.isBlank())) {
            return null;
        }
        if (locationId != null && locationName != null && !locationName.isBlank()) {
            throw new BadRequestException("LOCATION_AMBIGUOUS",
                    "Provide either locationId or locationName, not both");
        }
        if (locationId != null) {
            return locationRepository.findByIdAndOwnerId(locationId, ownerId)
                    .orElseThrow(() -> new NotFoundException("Location not found"));
        }
        return locationService.findOrCreate(ownerId, locationName);
    }

    private void validateForCreate(GarmentCreateRequest request) {
        if (request.status() == GarmentStatus.FOR_SALE && request.salePrice() == null) {
            throw new BadRequestException("MISSING_PRICE",
                    "Sale price is required when status is FOR_SALE");
        }
        if (request.status() == GarmentStatus.SOLD) {
            throw new BadRequestException("INVALID_STATUS",
                    "A new garment cannot be created as SOLD; create it and mark it as sold afterwards");
        }
        if (request.subcategory() != null && request.subcategory().getParent() != request.category()) {
            throw new BadRequestException("INVALID_SUBCATEGORY",
                    "Subcategory " + request.subcategory() + " does not belong to category " + request.category());
        }
    }

    private void validateForUpdate(GarmentStatus currentStatus, GarmentUpdateRequest request) {
        if (currentStatus == GarmentStatus.SOLD && request.status() == GarmentStatus.FOR_SALE) {
            throw new BadRequestException("INVALID_TRANSITION",
                    "Cannot move a SOLD garment directly to FOR_SALE; move it to wardrobe first");
        }
        if (request.status() == GarmentStatus.FOR_SALE && request.salePrice() == null) {
            throw new BadRequestException("MISSING_PRICE",
                    "Sale price is required when status is FOR_SALE");
        }
        if (request.subcategory() != null && request.subcategory().getParent() != request.category()) {
            throw new BadRequestException("INVALID_SUBCATEGORY",
                    "Subcategory " + request.subcategory() + " does not belong to category " + request.category());
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Aggregate counts of the user's wardrobe (status = WARDROBE only).
     * Returns the headline total plus per-category and per-subcategory
     * breakdowns; garments without a subcategory are counted under the
     * parent category bucket only.
     */
    @Transactional(readOnly = true)
    public WardrobeStatsResponse wardrobeStats(Long ownerId) {
        List<Object[]> rows = garmentRepository.countWardrobeByCategoryAndSubcategory(ownerId);

        Map<Category, Long> categoryTotals = new EnumMap<>(Category.class);
        Map<Category, Map<Subcategory, Long>> subsByCategory = new EnumMap<>(Category.class);
        List<WardrobeStatsResponse.Detail> details = new ArrayList<>();
        long total = 0;

        for (Object[] row : rows) {
            Category category = (Category) row[0];
            Subcategory subcategory = (Subcategory) row[1];
            long count = ((Number) row[2]).longValue();
            total += count;
            categoryTotals.merge(category, count, Long::sum);
            if (subcategory != null) {
                subsByCategory
                        .computeIfAbsent(category, k -> new EnumMap<>(Subcategory.class))
                        .merge(subcategory, count, Long::sum);
            }
            details.add(new WardrobeStatsResponse.Detail(category, subcategory, count));
        }

        details.sort(Comparator
                .comparing((WardrobeStatsResponse.Detail d) -> d.category().name())
                .thenComparing(d -> d.subcategory() == null ? "" : d.subcategory().name()));

        List<WardrobeStatsResponse.CategoryBucket> byCategory = new ArrayList<>();
        // Order categories by count desc, but keep a stable order for empty buckets
        categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> {
                    Category cat = entry.getKey();
                    Map<Subcategory, Long> subs = subsByCategory.getOrDefault(cat, Map.of());
                    List<WardrobeStatsResponse.SubcategoryBucket> subList = new ArrayList<>();
                    subs.entrySet().stream()
                            .sorted(Map.Entry.<Subcategory, Long>comparingByValue().reversed())
                            .forEach(sub -> subList.add(
                                    new WardrobeStatsResponse.SubcategoryBucket(sub.getKey(), sub.getValue())));
                    byCategory.add(new WardrobeStatsResponse.CategoryBucket(cat, entry.getValue(), subList));
                });

        return new WardrobeStatsResponse(total, byCategory, details);
    }
}
