package com.example.wardrobe.location;

import com.example.wardrobe.support.AbstractIntegrationTest;
import com.example.wardrobe.support.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocationIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort int port;
    @Autowired ObjectMapper objectMapper;

    private ApiClient newUser(String name) {
        ApiClient client = new ApiClient("http://localhost:" + port, objectMapper, name);
        client.postJson("/api/auth/users",
                Map.of("username", name, "password", "Password1!", "displayName", name));
        client.login("Password1!");
        return client;
    }

    @Test
    void crudLocations() {
        ApiClient client = newUser("loc1_" + System.nanoTime());

        ResponseEntity<JsonNode> created = client.postJson("/api/locations",
                Map.of("name", "Armario grande"));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().get("id").asLong();
        assertThat(created.getBody().get("name").asText()).isEqualTo("Armario grande");
        assertThat(created.getBody().get("garmentCount").asLong()).isZero();

        // List returns it
        ResponseEntity<JsonNode> list = client.getJson("/api/locations");
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().isArray()).isTrue();
        assertThat(list.getBody().size()).isEqualTo(1);

        // Rename
        ResponseEntity<JsonNode> renamed = client.putJson("/api/locations/" + id,
                Map.of("name", "Armario pequeño"));
        assertThat(renamed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(renamed.getBody().get("name").asText()).isEqualTo("Armario pequeño");

        // Delete (no garments using it)
        ResponseEntity<Void> deleted = client.delete("/api/locations/" + id);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JsonNode> listAfter = client.getJson("/api/locations");
        assertThat(listAfter.getBody().size()).isZero();
    }

    @Test
    void duplicateNameIsRejected() {
        ApiClient client = newUser("loc2_" + System.nanoTime());
        client.postJson("/api/locations", Map.of("name", "Bolsa 1"));
        ResponseEntity<JsonNode> dup = client.postJson("/api/locations", Map.of("name", "bolsa 1"));
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(dup.getBody().get("error").asText()).isEqualTo("LOCATION_NAME_TAKEN");
    }

    @Test
    void cannotDeleteLocationUsedByGarments() {
        ApiClient client = newUser("loc3_" + System.nanoTime());
        ResponseEntity<JsonNode> loc = client.postJson("/api/locations", Map.of("name", "Caja"));
        Long locationId = loc.getBody().get("id").asLong();

        client.postJson("/api/garments", Map.of(
                "name", "Camiseta", "size", "M",
                "category", "TOP", "subcategory", "T_SHIRT",
                "condition", "GOOD", "status", "WARDROBE",
                "season", "SUMMER",
                "locationId", locationId
        ));

        ResponseEntity<JsonNode> del;
        try {
            del = client.rawRest().exchange(
                    "/api/locations/" + locationId,
                    org.springframework.http.HttpMethod.DELETE,
                    new org.springframework.http.HttpEntity<>(client.authHeaders()),
                    JsonNode.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            del = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAs(JsonNode.class));
        }
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(del.getBody().get("error").asText()).isEqualTo("LOCATION_IN_USE");
    }

    @Test
    void garmentCreationByNameIsIdempotent() {
        ApiClient client = newUser("loc4_" + System.nanoTime());
        ResponseEntity<JsonNode> g1 = client.postJson("/api/garments", Map.of(
                "name", "Camiseta A", "size", "M",
                "category", "TOP", "subcategory", "T_SHIRT",
                "condition", "GOOD", "status", "WARDROBE",
                "season", "SUMMER",
                "locationName", "Cajón ropa interior"
        ));
        assertThat(g1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long loc1 = g1.getBody().get("locationId").asLong();

        ResponseEntity<JsonNode> g2 = client.postJson("/api/garments", Map.of(
                "name", "Camiseta B", "size", "M",
                "category", "TOP", "subcategory", "T_SHIRT",
                "condition", "GOOD", "status", "WARDROBE",
                "season", "SUMMER",
                "locationName", "cajón ropa interior"
        ));
        assertThat(g2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long loc2 = g2.getBody().get("locationId").asLong();
        assertThat(loc2).isEqualTo(loc1);
    }

    @Test
    void locationAmbiguityIsRejected() {
        ApiClient client = newUser("loc5_" + System.nanoTime());
        ResponseEntity<JsonNode> loc = client.postJson("/api/locations", Map.of("name", "Percha"));
        Long locationId = loc.getBody().get("id").asLong();

        ResponseEntity<JsonNode> bad = client.postJson("/api/garments", Map.of(
                "name", "Camiseta", "size", "M",
                "category", "TOP", "subcategory", "T_SHIRT",
                "condition", "GOOD", "status", "WARDROBE",
                "season", "SUMMER",
                "locationId", locationId,
                "locationName", "Otro"
        ));
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(bad.getBody().get("error").asText()).isEqualTo("LOCATION_AMBIGUOUS");
    }
}
