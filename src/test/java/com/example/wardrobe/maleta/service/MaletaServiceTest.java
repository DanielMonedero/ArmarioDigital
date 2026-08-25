package com.example.wardrobe.maleta.service;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.ConflictException;
import com.example.wardrobe.common.exception.NotFoundException;
import com.example.wardrobe.garment.entity.Category;
import com.example.wardrobe.garment.entity.Garment;
import com.example.wardrobe.garment.entity.GarmentCondition;
import com.example.wardrobe.garment.entity.GarmentStatus;
import com.example.wardrobe.garment.entity.Season;
import com.example.wardrobe.garment.repository.GarmentRepository;
import com.example.wardrobe.maleta.entity.MaletaItem;
import com.example.wardrobe.maleta.repository.MaletaItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaletaServiceTest {

    @Mock private MaletaItemRepository maletaItemRepository;
    @Mock private GarmentRepository garmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private MaletaService service;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User("alice", "hash", "Alice");
        owner.setId(1L);
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
    }

    private Garment wardrobeGarment(Long id, String name) {
        Garment g = new Garment();
        g.setId(id);
        g.setOwner(owner);
        g.setName(name);
        g.setSize("M");
        g.setCategory(Category.TOP);
        g.setCondition(GarmentCondition.GOOD);
        g.setStatus(GarmentStatus.WARDROBE);
        g.setSeason(Season.SUMMER);
        return g;
    }

    private Garment soldGarment(Long id) {
        Garment g = wardrobeGarment(id, "Vendida");
        g.setStatus(GarmentStatus.SOLD);
        return g;
    }

    @Test
    void addRejectsGarmentNotOwnedByUser() {
        when(garmentRepository.findByIdAndOwnerId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(1L, 10L))
                .isInstanceOf(NotFoundException.class);
        verify(maletaItemRepository, never()).save(any());
    }

    @Test
    void addRejectsGarmentNotInWardrobeStatus() {
        when(garmentRepository.findByIdAndOwnerId(20L, 1L))
                .thenReturn(Optional.of(soldGarment(20L)));

        assertThatThrownBy(() -> service.add(1L, 20L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Armario");
        verify(maletaItemRepository, never()).save(any());
    }

    @Test
    void addRejectsDuplicate() {
        when(maletaItemRepository.existsByOwnerIdAndGarmentId(1L, 30L)).thenReturn(true);

        assertThatThrownBy(() -> service.add(1L, 30L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya est");
        verify(maletaItemRepository, never()).save(any());
    }

    @Test
    void addPersistsAndReturnsItem() {
        Garment g = wardrobeGarment(40L, "Camiseta");
        when(maletaItemRepository.existsByOwnerIdAndGarmentId(1L, 40L)).thenReturn(false);
        when(garmentRepository.findByIdAndOwnerId(40L, 1L)).thenReturn(Optional.of(g));
        when(maletaItemRepository.save(any(MaletaItem.class))).thenAnswer(inv -> {
            MaletaItem item = inv.getArgument(0);
            item.setId(99L);
            return item;
        });

        var response = service.add(1L, 40L);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.garmentId()).isEqualTo(40L);
        assertThat(response.garment().name()).isEqualTo("Camiseta");
    }

    @Test
    void removeThrowsWhenNotPresent() {
        when(maletaItemRepository.findByOwnerIdAndGarmentId(1L, 50L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(1L, 50L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void clearReturnsCountBeforeDeletion() {
        when(maletaItemRepository.countByOwnerId(1L)).thenReturn(7L);

        int cleared = service.clear(1L);

        assertThat(cleared).isEqualTo(7);
        verify(maletaItemRepository).deleteByOwnerId(1L);
    }

    @Test
    void listDelegatesToRepository() {
        MaletaItem a = new MaletaItem(owner, wardrobeGarment(1L, "a"));
        a.setId(1L);
        MaletaItem b = new MaletaItem(owner, wardrobeGarment(2L, "b"));
        b.setId(2L);
        when(maletaItemRepository.findByOwnerIdOrderByAddedAtDesc(1L))
                .thenReturn(List.of(a, b));

        var result = service.list(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).garmentId()).isEqualTo(1L);
        assertThat(result.get(1).garmentId()).isEqualTo(2L);
    }
}