package com.example.wardrobe.garment;

import com.example.wardrobe.support.AbstractIntegrationTest;
import com.example.wardrobe.support.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GarmentIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort int port;
    @Autowired ObjectMapper objectMapper;

    private ApiClient newUser(String name) {
        ApiClient client = new ApiClient("http://localhost:" + port, objectMapper, name);
        ResponseEntity<JsonNode> created = client.postJson("/api/auth/users",
                Map.of("username", name, "password", "Password1!", "displayName", name));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        client.login("Password1!");
        return client;
    }

    private Long createGarment(ApiClient client, String name, String status) {
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", name,
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "color", "BLUE",
                "condition", "GOOD",
                "status", status,
                "season", "SUMMER",
                "salePrice", new BigDecimal("19.99"),
                "brand", "Nike"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().get("id").asLong();
    }

    @Test
    void createsGarmentOwnedByCurrentUser() {
        ApiClient client = newUser("alice_" + System.nanoTime());
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "color", "RED",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("name").asText()).isEqualTo("Camiseta");
        assertThat(response.getBody().get("status").asText()).isEqualTo("WARDROBE");
        assertThat(response.getBody().get("subcategory").asText()).isEqualTo("T_SHIRT");
        assertThat(response.getBody().get("color").asText()).isEqualTo("RED");
        assertThat(response.getBody().get("season").asText()).isEqualTo("SUMMER");
    }

    @Test
    void rejectsCreationWithMissingRequiredFields() {
        ApiClient client = newUser("bob_" + System.nanoTime());
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "size", "M",
                "category", "TOP",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("fieldErrors").get("name")).isNotNull();
    }

    @Test
    void rejectsCreationWithMissingSeason() {
        ApiClient client = newUser("bob2_" + System.nanoTime());
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "condition", "GOOD",
                "status", "WARDROBE"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("fieldErrors").get("season")).isNotNull();
    }

    @Test
    void rejectsMismatchedSubcategory() {
        ApiClient client = newUser("bob3_" + System.nanoTime());
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", "Jeans",
                "size", "32",
                "category", "TOP",
                "subcategory", "JEANS",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "WINTER"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error").asText()).isEqualTo("INVALID_SUBCATEGORY");
    }

    @Test
    void rejectsCreateWithForSaleAndNoPrice() {
        ApiClient client = newUser("carol_" + System.nanoTime());
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "condition", "GOOD",
                "status", "FOR_SALE",
                "season", "SUMMER"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void otherUserCannotAccessGarment() {
        ApiClient alice = newUser("alice2_" + System.nanoTime());
        ApiClient mallory = newUser("mallory_" + System.nanoTime());

        Long id = createGarment(alice, "Camiseta", "WARDROBE");

        ResponseEntity<JsonNode> get = mallory.getJson("/api/garments/" + id);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<JsonNode> update = mallory.putJson("/api/garments/" + id, Map.of(
                "name", "Pwned",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER"
        ));
        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Void> delete = mallory.delete("/api/garments/" + id);
        assertThat(delete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<JsonNode> aliceAfter = alice.getJson("/api/garments/" + id);
        assertThat(aliceAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aliceAfter.getBody().get("name").asText()).isEqualTo("Camiseta");
    }

    @Test
    void putForSaleRequiresImage() {
        ApiClient client = newUser("dave_" + System.nanoTime());
        Long id = createGarment(client, "Camiseta", "WARDROBE");
        client.putJson("/api/garments/" + id, Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER",
                "salePrice", new BigDecimal("15.00")
        ));
        ResponseEntity<JsonNode> response = client.postJson("/api/garments/" + id + "/put-for-sale", Map.of());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void fullLifecycleWardrobeForSaleSoldWardrobe() {
        ApiClient client = newUser("eve_" + System.nanoTime());

        ResponseEntity<JsonNode> created = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "color", "BLUE",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER",
                "brand", "Nike",
                "salePrice", new BigDecimal("19.99")
        ));
        Long id = created.getBody().get("id").asLong();

        org.springframework.http.HttpEntity<org.springframework.core.io.Resource> imageEntity =
                uploadTestImage(client, id, "image/jpeg", ".jpg");

        ResponseEntity<JsonNode> putForSale = client.postJson(
                "/api/garments/" + id + "/put-for-sale", Map.of());
        assertThat(putForSale.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putForSale.getBody().get("status").asText()).isEqualTo("FOR_SALE");
        assertThat(putForSale.getBody().get("description").asText())
                .isEqualTo("Camiseta Nike blue, talla M. En buen estado.");

        ResponseEntity<JsonNode> sold = client.postJson(
                "/api/garments/" + id + "/mark-as-sold", Map.of());
        assertThat(sold.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sold.getBody().get("status").asText()).isEqualTo("SOLD");
        assertThat(sold.getBody().get("soldAt").asText()).isNotEmpty();

        ResponseEntity<JsonNode> back = client.postJson(
                "/api/garments/" + id + "/move-to-wardrobe", Map.of());
        assertThat(back.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(back.getBody().get("status").asText()).isEqualTo("WARDROBE");
        assertThat(back.getBody().get("soldAt").isNull()).isTrue();
        assertThat(back.getBody().get("salePrice").decimalValue()).isEqualByComparingTo("19.99");
    }

    @Test
    void filtersByCategoryAndSubcategory() {
        ApiClient client = newUser("frank_" + System.nanoTime());
        ResponseEntity<JsonNode> shirt = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER"
        ));
        ResponseEntity<JsonNode> pants = client.postJson("/api/garments", Map.of(
                "name", "Pantalón",
                "size", "32",
                "category", "BOTTOM",
                "subcategory", "JEANS",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "WINTER"
        ));
        Long pantsId = pants.getBody().get("id").asLong();
        Long shirtId = shirt.getBody().get("id").asLong();

        ResponseEntity<JsonNode> response = client.getJson(
                "/api/garments?category=BOTTOM&subcategory=JEANS");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        boolean foundPants = false;
        boolean foundShirt = false;
        for (JsonNode n : response.getBody().get("content")) {
            if (n.get("id").asLong() == pantsId) foundPants = true;
            if (n.get("id").asLong() == shirtId) foundShirt = true;
        }
        assertThat(foundPants).isTrue();
        assertThat(foundShirt).isFalse();
    }

    @Test
    void filtersByColorAndSeason() {
        ApiClient client = newUser("frank2_" + System.nanoTime());
        Long summerRed = client.postJson("/api/garments", Map.of(
                "name", "Camiseta roja",
                "size", "M",
                "category", "TOP",
                "subcategory", "T_SHIRT",
                "color", "RED",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "SUMMER"
        )).getBody().get("id").asLong();
        Long winterBlue = client.postJson("/api/garments", Map.of(
                "name", "Jersey azul",
                "size", "L",
                "category", "SWEATER",
                "subcategory", "SWEATER",
                "color", "BLUE",
                "condition", "GOOD",
                "status", "WARDROBE",
                "season", "WINTER"
        )).getBody().get("id").asLong();

        ResponseEntity<JsonNode> redResp = client.getJson("/api/garments?color=RED&season=SUMMER");
        assertThat(redResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean foundRed = false;
        boolean foundBlue = false;
        for (JsonNode n : redResp.getBody().get("content")) {
            if (n.get("id").asLong() == summerRed) foundRed = true;
            if (n.get("id").asLong() == winterBlue) foundBlue = true;
        }
        assertThat(foundRed).isTrue();
        assertThat(foundBlue).isFalse();
    }

    @Test
    void paginatesResults() {
        ApiClient client = newUser("gina_" + System.nanoTime());
        for (int i = 0; i < 5; i++) {
            createGarment(client, "Item " + i, "WARDROBE");
        }
        ResponseEntity<JsonNode> page0 = client.getJson("/api/garments?page=0&size=2");
        assertThat(page0.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page0.getBody().get("content").size()).isEqualTo(2);
        assertThat(page0.getBody().get("totalElements").asInt()).isEqualTo(5);
        assertThat(page0.getBody().get("totalPages").asInt()).isEqualTo(3);
    }

    @Test
    void searchFindsByName() {
        ApiClient client = newUser("hank_" + System.nanoTime());
        createGarment(client, "Camiseta rara", "WARDROBE");
        createGarment(client, "Pantalón", "WARDROBE");

        ResponseEntity<JsonNode> response = client.getJson("/api/garments?search=rara");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean found = false;
        for (JsonNode n : response.getBody().get("content")) {
            if (n.get("name").asText().contains("rara")) found = true;
        }
        assertThat(found).isTrue();
    }

    @Test
    void cannotMarkAsSoldFromWardrobe() {
        ApiClient client = newUser("ivy_" + System.nanoTime());
        Long id = createGarment(client, "Camiseta", "WARDROBE");
        ResponseEntity<JsonNode> response = client.postJson(
                "/api/garments/" + id + "/mark-as-sold", Map.of());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private org.springframework.http.HttpEntity<org.springframework.core.io.Resource> uploadTestImage(
            ApiClient client, Long garmentId, String contentType, String ext) {
        org.springframework.core.io.ByteArrayResource resource =
                new org.springframework.core.io.ByteArrayResource(new byte[]{1, 2, 3, 4}) {
                    @Override
                    public String getFilename() {
                        return "photo" + ext;
                    }
                };
        org.springframework.http.HttpHeaders headers = client.authHeaders();
        org.springframework.util.MultiValueMap<String, Object> body =
                new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", resource);
        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> request =
                new org.springframework.http.HttpEntity<>(body, headers);
        try {
            ResponseEntity<JsonNode> response = client.rawRest().exchange(
                    "/api/garments/" + garmentId + "/images",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    JsonNode.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
