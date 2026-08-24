package com.example.wardrobe.garment.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.description.DescriptionGenerator;
import com.example.wardrobe.garment.dto.GarmentCreateRequest;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Color;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.entity.Subcategory;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.image.entity.GarmentImage;
import com.example.wardrobe.image.repository.GarmentImageRepository;
import com.example.wardrobe.image.storage.FileSystemStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarmentServiceTest {

    @Mock private GarmentRepository garmentRepository;
    @Mock private GarmentImageRepository garmentImageRepository;
    @Mock private UserRepository userRepository;
    @Mock private com.example.wardrobe.location.repository.LocationRepository locationRepository;
    @Mock private com.example.wardrobe.location.service.LocationService locationService;
    @Mock private DescriptionGenerator descriptionGenerator;
    @Mock private FileSystemStorage fileSystemStorage;

    @InjectMocks private GarmentService service;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User("alice", "hash", "Alice");
        owner.setId(1L);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    }

    @Test
    void createStoresGarmentOwnedByCurrentUser() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Camiseta", null, "M", Category.TOP, Subcategory.T_SHIRT, Color.BLUE, "Nike",
                GarmentCondition.GOOD, GarmentStatus.WARDROBE, Season.SUMMER, null, null, null, null, null
        );
        when(garmentRepository.save(any(Garment.class))).thenAnswer(inv -> {
            Garment g = inv.getArgument(0);
            g.setId(42L);
            return g;
        });

        var response = service.create(1L, request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(GarmentStatus.WARDROBE);
        verify(garmentRepository).save(any(Garment.class));
    }

    @Test
    void createWithForSaleRequiresSalePrice() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Camiseta", null, "M", Category.TOP, Subcategory.T_SHIRT, Color.BLUE, "Nike",
                GarmentCondition.GOOD, GarmentStatus.FOR_SALE, Season.SUMMER, null, null, null, null, null
        );
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Sale price");
    }

    @Test
    void createAsSoldIsRejected() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Camiseta", null, "M", Category.TOP, Subcategory.T_SHIRT, Color.BLUE, "Nike",
                GarmentCondition.GOOD, GarmentStatus.SOLD, Season.SUMMER,
                new BigDecimal("10.00"), null, null, null, null
        );
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createWithMismatchedSubcategoryIsRejected() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Jeans", null, "32", Category.TOP, Subcategory.JEANS, Color.BLUE, null,
                GarmentCondition.GOOD, GarmentStatus.WARDROBE, Season.WINTER, null, null, null, null, null
        );
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Subcategory");
    }

    @Test
    void createWithLocationNameDelegatesToLocationService() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Camiseta", null, "M", Category.TOP, Subcategory.T_SHIRT, Color.BLUE, null,
                GarmentCondition.GOOD, GarmentStatus.WARDROBE, Season.SUMMER,
                null, null, null, null, "Armario"
        );
        com.example.wardrobe.location.entity.Location loc =
                new com.example.wardrobe.location.entity.Location(owner, "Armario");
        loc.setId(7L);
        when(locationService.findOrCreate(1L, "Armario")).thenReturn(loc);
        when(garmentRepository.save(any(Garment.class))).thenAnswer(inv -> {
            Garment g = inv.getArgument(0);
            g.setId(42L);
            return g;
        });

        var response = service.create(1L, request);

        assertThat(response.locationId()).isEqualTo(7L);
        assertThat(response.locationName()).isEqualTo("Armario");
        verify(locationService).findOrCreate(1L, "Armario");
    }

    @Test
    void createWithAmbiguousLocationIsRejected() {
        GarmentCreateRequest request = new GarmentCreateRequest(
                "Camiseta", null, "M", Category.TOP, Subcategory.T_SHIRT, Color.BLUE, null,
                GarmentCondition.GOOD, GarmentStatus.WARDROBE, Season.SUMMER,
                null, null, null, 5L, "Armario"
        );
        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("LOCATION_AMBIGUOUS");
    }

    @Test
    void putForSaleRequiresExistingImage() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("19.99"));
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        assertThatThrownBy(() -> service.putForSale(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("image");
        verify(descriptionGenerator, never()).generate(any());
    }

    @Test
    void putForSaleRequiresSalePrice() {
        Garment garment = wardrobe();
        garment.getImages().add(image(garment));
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        assertThatThrownBy(() -> service.putForSale(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("sale price");
    }

    @Test
    void putForSaleChangesStatusAndGeneratesDescription() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("29.95"));
        garment.getImages().add(image(garment));
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));
        when(descriptionGenerator.generate(any())).thenReturn("Camiseta Nike blue, talla M. En buen estado.");

        var response = service.putForSale(1L, 10L);

        assertThat(response.status()).isEqualTo(GarmentStatus.FOR_SALE);
        assertThat(garment.getDescription()).isEqualTo("Camiseta Nike blue, talla M. En buen estado.");
        verify(descriptionGenerator).generate(garment);
    }

    @Test
    void putForSaleOnSoldGarmentIsRejected() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("29.95"));
        garment.getImages().add(image(garment));
        garment.setStatus(GarmentStatus.SOLD);
        garment.setSoldAt(java.time.Instant.now());
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        assertThatThrownBy(() -> service.putForSale(1L, 10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void markAsSoldRequiresForSaleStatus() {
        Garment garment = wardrobe();
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        assertThatThrownBy(() -> service.markAsSold(1L, 10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void markAsSoldTransitionsAndSetsTimestamp() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("29.95"));
        garment.setStatus(GarmentStatus.FOR_SALE);
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        var response = service.markAsSold(1L, 10L);

        assertThat(response.status()).isEqualTo(GarmentStatus.SOLD);
        assertThat(garment.getSoldAt()).isNotNull();
    }

    @Test
    void moveToWardrobeClearsSoldAt() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("29.95"));
        garment.setStatus(GarmentStatus.SOLD);
        garment.setSoldAt(java.time.Instant.now());
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        var response = service.moveToWardrobe(1L, 10L);

        assertThat(response.status()).isEqualTo(GarmentStatus.WARDROBE);
        assertThat(garment.getSoldAt()).isNull();
    }

    @Test
    void ownershipPreventsAccessByOtherUser() {
        when(garmentRepository.findByIdAndOwnerId(10L, 99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L, 10L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteRemovesImageFilesAndGarment() {
        Garment garment = wardrobeWithSalePrice(new BigDecimal("29.95"));
        GarmentImage img = image(garment);
        garment.getImages().add(img);
        when(garmentRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(garment));

        service.delete(1L, 10L);

        verify(fileSystemStorage).delete(img.getFilename());
        verify(garmentRepository).delete(garment);
    }

    private Garment wardrobe() {
        Garment g = new Garment();
        g.setId(10L);
        g.setOwner(owner);
        g.setName("Camiseta");
        g.setSize("M");
        g.setCategory(Category.TOP);
        g.setSubcategory(Subcategory.T_SHIRT);
        g.setColor(Color.BLUE);
        g.setCondition(GarmentCondition.GOOD);
        g.setStatus(GarmentStatus.WARDROBE);
        g.setSeason(Season.SUMMER);
        g.setImages(new ArrayList<>());
        return g;
    }

    private Garment wardrobeWithSalePrice(BigDecimal price) {
        Garment g = wardrobe();
        g.setSalePrice(price);
        return g;
    }

    private GarmentImage image(Garment garment) {
        GarmentImage img = new GarmentImage();
        img.setId(99L);
        img.setGarment(garment);
        img.setFilename("abc.jpg");
        img.setOriginalFilename("photo.jpg");
        img.setContentType("image/jpeg");
        img.setSize(1024);
        img.setSortOrder(0);
        return img;
    }
}
