package com.example.wardrobe.image;

import com.example.wardrobe.support.AbstractIntegrationTest;
import com.example.wardrobe.support.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImageIntegrationTest extends AbstractIntegrationTest {

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

    private Long createGarment(ApiClient client) {
        ResponseEntity<JsonNode> response = client.postJson("/api/garments", Map.of(
                "name", "Camiseta",
                "size", "M",
                "category", "T_SHIRT",
                "condition", "GOOD",
                "status", "WARDROBE"
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().get("id").asLong();
    }

    private JsonNode upload(ApiClient client, Long garmentId, byte[] data, String contentType, String filename) {
        ByteArrayResource resource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        HttpHeaders headers = client.authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = client.rawRest().exchange(
                "/api/garments/" + garmentId + "/images",
                HttpMethod.POST, req, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void uploadsAndDownloadsImage() throws Exception {
        ApiClient client = newUser("img_" + System.nanoTime());
        Long garmentId = createGarment(client);

        JsonNode uploaded = upload(client, garmentId, new byte[]{1, 2, 3, 4},
                "image/jpeg", "photo.jpg");
        Long imageId = uploaded.get("id").asLong();
        assertThat(uploaded.get("contentType").asText()).isEqualTo("image/jpeg");
        assertThat(uploaded.get("originalFilename").asText()).isEqualTo("photo.jpg");
        assertThat(uploaded.get("filename").asText()).isNotEqualTo("photo.jpg");

        ResponseEntity<byte[]> download = client.rawRest().exchange(
                "/api/garments/" + garmentId + "/images/" + imageId,
                HttpMethod.GET, new HttpEntity<>(client.authHeaders()),
                byte[].class);
        assertThat(download.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(download.getHeaders().getContentType().toString()).startsWith("image/jpeg");
        assertThat(download.getBody()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void rejectsUnsupportedContentType() {
        ApiClient client = newUser("imgb_" + System.nanoTime());
        Long garmentId = createGarment(client);

        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2}) {
            @Override
            public String getFilename() {
                return "file.txt";
            }
        };
        HttpHeaders headers = client.authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = client.rawRest().exchange(
                "/api/garments/" + garmentId + "/images",
                HttpMethod.POST, req, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsAccessFromAnotherUser() {
        ApiClient alice = newUser("alicei_" + System.nanoTime());
        ApiClient bob = newUser("bobi_" + System.nanoTime());

        Long garmentId = createGarment(alice);
        JsonNode uploaded = upload(alice, garmentId, new byte[]{9, 9, 9},
                "image/png", "photo.png");
        Long imageId = uploaded.get("id").asLong();

        ResponseEntity<byte[]> download = bob.rawRest().exchange(
                "/api/garments/" + garmentId + "/images/" + imageId,
                HttpMethod.GET, new HttpEntity<>(bob.authHeaders()),
                byte[].class);
        assertThat(download.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void cannotDeleteLastImage() {
        ApiClient client = newUser("imc_" + System.nanoTime());
        Long garmentId = createGarment(client);
        JsonNode uploaded = upload(client, garmentId, new byte[]{1},
                "image/jpeg", "photo.jpg");
        Long imageId = uploaded.get("id").asLong();

        ResponseEntity<Void> response = client.delete(
                "/api/garments/" + garmentId + "/images/" + imageId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteAndReorderImages() {
        ApiClient client = newUser("imd_" + System.nanoTime());
        Long garmentId = createGarment(client);

        JsonNode img1 = upload(client, garmentId, new byte[]{1}, "image/jpeg", "a.jpg");
        JsonNode img2 = upload(client, garmentId, new byte[]{2}, "image/jpeg", "b.jpg");
        JsonNode img3 = upload(client, garmentId, new byte[]{3}, "image/jpeg", "c.jpg");

        Long id1 = img1.get("id").asLong();
        Long id2 = img2.get("id").asLong();
        Long id3 = img3.get("id").asLong();

        ResponseEntity<JsonNode> reordered = client.putJson(
                "/api/garments/" + garmentId + "/images/order",
                Map.of("imageIds", java.util.List.of(id3, id2, id1)));
        assertThat(reordered.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reordered.getBody().get(0).get("id").asLong()).isEqualTo(id3);

        ResponseEntity<Void> deleteOne = client.delete(
                "/api/garments/" + garmentId + "/images/" + id1);
        assertThat(deleteOne.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<JsonNode> detail = client.getJson("/api/garments/" + garmentId);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detail.getBody().get("images").size()).isEqualTo(2);
    }
}
