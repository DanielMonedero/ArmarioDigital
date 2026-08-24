package com.example.wardrobe.auth;

import com.example.wardrobe.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    private RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .rootUri("http://localhost:" + port)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String createUser(String username, String password) {
        try {
            ResponseEntity<JsonNode> response = restTemplate().postForEntity(
                    "/api/auth/users",
                    Map.of("username", username, "password", password, "displayName", username),
                    JsonNode.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            return response.getBody().get("username").asText();
        } catch (HttpClientErrorException e) {
            throw new AssertionError("Failed to create user: " + e.getResponseBodyAsString(), e);
        }
    }

    @Test
    void loginReturnsUserAndSessionCookie() throws Exception {
        String username = "auth_" + System.nanoTime();
        createUser(username, "Password1!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(Map.of("username", username, "password", "Password1!")),
                headers
        );

        ResponseEntity<JsonNode> response = restTemplate().exchange(
                "/api/auth/login", HttpMethod.POST, request, JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("user").get("username").asText()).isEqualTo(username);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotEmpty();
        assertThat(cookies.stream().anyMatch(c -> c.startsWith("JSESSIONID"))).isTrue();
    }

    @Test
    void meReturnsAuthenticatedUserWithoutPasswordHash() {
        String username = "me_" + System.nanoTime();
        createUser(username, "Password1!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> login = new HttpEntity<>(
                "{\"username\":\"" + username + "\",\"password\":\"Password1!\"}",
                headers
        );
        ResponseEntity<JsonNode> loginResponse = restTemplate().exchange(
                "/api/auth/login", HttpMethod.POST, login, JsonNode.class
        );
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.add(HttpHeaders.COOKIE, String.join("; ",
                loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE)));

        ResponseEntity<JsonNode> me = restTemplate().exchange(
                "/api/auth/me", HttpMethod.GET, new HttpEntity<>(sessionHeaders), JsonNode.class
        );

        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().get("username").asText()).isEqualTo(username);
        assertThat(me.getBody().has("password")).isFalse();
        assertThat(me.getBody().has("passwordHash")).isFalse();
        assertThat(me.getBody().has("password_hash")).isFalse();
    }

    @Test
    void meWithoutSessionReturnsUnauthorized() {
        ResponseEntity<String> response;
        try {
            response = restTemplate().getForEntity("/api/auth/me", String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            return;
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithBadCredentialsReturns401() {
        String username = "bad_" + System.nanoTime();
        createUser(username, "Password1!");

        try {
            restTemplate().postForEntity(
                    "/api/auth/login",
                    Map.of("username", username, "password", "WrongPassword!"),
                    String.class
            );
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void createUserWithDuplicateUsernameReturns409() {
        String username = "dup_" + System.nanoTime();
        createUser(username, "Password1!");

        try {
            restTemplate().postForEntity(
                    "/api/auth/users",
                    Map.of("username", username, "password", "Another1!"),
                    String.class
            );
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }
}
