package com.example.wardrobe.common.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public, non-authenticated endpoint that exposes non-sensitive client
 * configuration. Lets the frontend decide whether to show optional UI
 * (e.g. the "create user" form on the login page) based on server flags
 * rather than guessing from build-time environment.
 */
@RestController
@RequestMapping("/api/config")
public class PublicConfigController {

    private final AppProperties appProperties;

    public PublicConfigController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public Map<String, Object> get() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("allowUserCreation", appProperties.getAuth().isAllowUserCreation());
        return body;
    }
}
