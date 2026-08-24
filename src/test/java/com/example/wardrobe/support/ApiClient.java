package com.example.wardrobe.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ApiClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String username;
    private String sessionCookie;

    public ApiClient(String baseUrl, ObjectMapper objectMapper, String username) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplateBuilder()
                .rootUri(baseUrl)
                .errorHandler(new NoOpErrorHandler())
                .build();
        this.username = username;
    }

    public String username() {
        return username;
    }

    public ApiClient login(String password) {
        HttpHeaders h = jsonHeaders();
        HttpEntity<String> req = new HttpEntity<>(
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                h
        );
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/auth/login", HttpMethod.POST, req, JsonNode.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Login failed: " + response.getStatusCode());
        }
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        if (setCookie != null) {
            int semi = setCookie.indexOf(';');
            this.sessionCookie = semi > 0 ? setCookie.substring(0, semi) : setCookie;
        }
        return this;
    }

    public HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        if (sessionCookie != null) {
            h.add(HttpHeaders.COOKIE, sessionCookie);
        }
        return h;
    }

    public HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    public String jsonBody(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<JsonNode> postJson(String path, Object body) {
        HttpHeaders h = jsonHeaders();
        if (sessionCookie != null) h.add(HttpHeaders.COOKIE, sessionCookie);
        try {
            return restTemplate.exchange(path, HttpMethod.POST,
                    new HttpEntity<>(jsonBody(body), h), JsonNode.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(null);
        }
    }

    public ResponseEntity<JsonNode> putJson(String path, Object body) {
        HttpHeaders h = jsonHeaders();
        if (sessionCookie != null) h.add(HttpHeaders.COOKIE, sessionCookie);
        try {
            return restTemplate.exchange(path, HttpMethod.PUT,
                    new HttpEntity<>(jsonBody(body), h), JsonNode.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(null);
        }
    }

    public ResponseEntity<JsonNode> getJson(String path) {
        HttpHeaders h = new HttpHeaders();
        if (sessionCookie != null) h.add(HttpHeaders.COOKIE, sessionCookie);
        try {
            return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(h), JsonNode.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(null);
        }
    }

    public ResponseEntity<Void> delete(String path) {
        HttpHeaders h = new HttpHeaders();
        if (sessionCookie != null) h.add(HttpHeaders.COOKIE, sessionCookie);
        try {
            return restTemplate.exchange(path, HttpMethod.DELETE, new HttpEntity<>(h), Void.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .build();
        }
    }

    public RestTemplate rawRest() {
        return restTemplate;
    }

    public Map<String, Object> loginBody(String password) {
        return Map.of("username", username, "password", password);
    }
}
