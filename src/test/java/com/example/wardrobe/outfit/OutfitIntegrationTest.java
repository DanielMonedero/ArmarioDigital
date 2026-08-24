package com.example.wardrobe.outfit;

import com.example.wardrobe.support.AbstractIntegrationTest;
import com.example.wardrobe.support.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OutfitIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort int port;
    @Autowired ObjectMapper objectMapper;

    private ApiClient newUser(String name) {
        ApiClient client = new ApiClient("http://localhost:" + port, objectMapper, name);
        client.postJson("/api/auth/users",
                Map.of("username", name, "password", "Password1!", "displayName", name));
        client.login("Password1!");
        return client;
    }

    private Long makeGarment(ApiClient client, String name, String category, String subcategory,
                             String season, String status) {
        ResponseEntity<JsonNode> r = client.postJson("/api/garments", Map.of(
                "name", name,
                "size", "M",
                "category", category,
                "subcategory", subcategory,
                "condition", "GOOD",
                "status", status,
                "season", season
        ));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return r.getBody().get("id").asLong();
    }

    @Test
    void generatesOutfitWithDress() {
        ApiClient client = newUser("outfit1_" + System.nanoTime());
        makeGarment(client, "V1", "DRESS", "LONG_DRESS", "SUMMER", "WARDROBE");
        makeGarment(client, "Z1", "SHOES", "SNEAKERS", "SUMMER", "WARDROBE");

        // Run several times to exercise the dress vs top+bottom branch.
        boolean sawDress = false;
        for (int i = 0; i < 8 && !sawDress; i++) {
            ResponseEntity<JsonNode> r = client.postJson("/api/outfits/generate", Map.of(
                    "season", "SUMMER",
                    "includeOuterwear", false,
                    "includeAccessories", false
            ));
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
            for (JsonNode item : r.getBody()) {
                if ("DRESS".equals(item.get("category").asText())) sawDress = true;
            }
        }
        assertThat(sawDress).isTrue();
    }

    @Test
    void generatesOutfitWithTopAndBottom() {
        ApiClient client = newUser("outfit2_" + System.nanoTime());
        // Force top+bottom branch by not providing any dresses.
        makeGarment(client, "T1", "TOP", "T_SHIRT", "WINTER", "WARDROBE");
        makeGarment(client, "B1", "BOTTOM", "JEANS", "WINTER", "WARDROBE");
        makeGarment(client, "Z1", "SHOES", "BOOTS", "WINTER", "WARDROBE");
        makeGarment(client, "O1", "OUTERWEAR", "JACKET", "WINTER", "WARDROBE");
        makeGarment(client, "A1", "ACCESSORIES", "BELT", "WINTER", "WARDROBE");

        ResponseEntity<JsonNode> r = client.postJson("/api/outfits/generate", Map.of(
                "season", "WINTER",
                "includeOuterwear", true,
                "includeAccessories", true
        ));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> categories = new java.util.ArrayList<>();
        r.getBody().forEach(n -> categories.add(n.get("category").asText()));
        assertThat(categories).contains("TOP", "BOTTOM", "SHOES", "OUTERWEAR");
        assertThat(categories).contains("ACCESSORIES");
    }

    @Test
    void rejectsWhenNoGarmentsForSeason() {
        ApiClient client = newUser("outfit3_" + System.nanoTime());
        // Only SUMMER garments exist.
        makeGarment(client, "T1", "TOP", "T_SHIRT", "SUMMER", "WARDROBE");

        ResponseEntity<JsonNode> r = client.postJson("/api/outfits/generate", Map.of(
                "season", "WINTER",
                "includeOuterwear", false,
                "includeAccessories", false
        ));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(r.getBody().get("error").asText()).isEqualTo("INSUFFICIENT_GARMENTS");
    }

    @Test
    void rejectsWhenNoShoes() {
        ApiClient client = newUser("outfit4_" + System.nanoTime());
        makeGarment(client, "V1", "DRESS", "LONG_DRESS", "SUMMER", "WARDROBE");
        // No shoes.

        ResponseEntity<JsonNode> r = client.postJson("/api/outfits/generate", Map.of(
                "season", "SUMMER",
                "includeOuterwear", false,
                "includeAccessories", false
        ));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void savesAndListsAndDeletesOutfit() {
        ApiClient client = newUser("outfit5_" + System.nanoTime());
        Long top = makeGarment(client, "Camiseta", "TOP", "T_SHIRT", "SUMMER", "WARDROBE");
        Long bottom = makeGarment(client, "Pantalón", "BOTTOM", "JEANS", "SUMMER", "WARDROBE");
        Long shoes = makeGarment(client, "Zapas", "SHOES", "SNEAKERS", "SUMMER", "WARDROBE");

        ResponseEntity<JsonNode> saved = client.postJson("/api/outfits", Map.of(
                "name", "Look casual",
                "season", "SUMMER",
                "includeOuterwear", false,
                "includeAccessories", false,
                "garmentIds", List.of(top, bottom, shoes)
        ));
        assertThat(saved.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long outfitId = saved.getBody().get("id").asLong();
        assertThat(saved.getBody().get("items").size()).isEqualTo(3);

        ResponseEntity<JsonNode> list = client.getJson("/api/outfits");
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().size()).isEqualTo(1);

        ResponseEntity<JsonNode> one = client.getJson("/api/outfits/" + outfitId);
        assertThat(one.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(one.getBody().get("name").asText()).isEqualTo("Look casual");

        ResponseEntity<Void> del = client.delete("/api/outfits/" + outfitId);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(client.getJson("/api/outfits").getBody().size()).isZero();
    }

    @Test
    void rejectsSeasonMismatchOnSave() {
        ApiClient client = newUser("outfit6_" + System.nanoTime());
        Long winter = makeGarment(client, "Abrigo", "OUTERWEAR", "COAT", "WINTER", "WARDROBE");
        Long summer = makeGarment(client, "Camiseta", "TOP", "T_SHIRT", "SUMMER", "WARDROBE");

        ResponseEntity<JsonNode> r = client.postJson("/api/outfits", Map.of(
                "name", "Mezcla",
                "season", "SUMMER",
                "includeOuterwear", false,
                "includeAccessories", false,
                "garmentIds", List.of(winter, summer)
        ));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().get("error").asText()).isEqualTo("SEASON_MISMATCH");
    }
}
