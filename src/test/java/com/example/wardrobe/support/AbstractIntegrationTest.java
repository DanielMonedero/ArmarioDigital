package com.example.wardrobe.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16-alpine");

    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        PostgreSQLContainer<?> existing = PostgresHolder.INSTANCE;
        POSTGRES = existing;
    }

    static final class PostgresHolder {
        static final PostgreSQLContainer<?> INSTANCE = create();

        private static PostgreSQLContainer<?> create() {
            PostgreSQLContainer<?> c = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName("wardrobe_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
            c.start();
            Runtime.getRuntime().addShutdownHook(new Thread(c::stop));
            return c;
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
