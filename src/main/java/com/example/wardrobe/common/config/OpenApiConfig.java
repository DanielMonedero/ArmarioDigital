package com.example.wardrobe.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wardrobeOpenAPI() {
        SecurityScheme sessionScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID");

        return new OpenAPI()
                .info(new Info()
                        .title("Wardrobe API")
                        .version("0.1.0")
                        .description("Backend for a multi-user clothing inventory application"))
                .components(new Components().addSecuritySchemes("session", sessionScheme))
                .addSecurityItem(new SecurityRequirement().addList("session"));
    }
}
