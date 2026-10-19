package com.purohitcommunity.portal.integration;

import com.purohitcommunity.portal.config.DataInitializer;
import com.purohitcommunity.portal.domain.Enums.PlanCode;
import com.purohitcommunity.portal.domain.Enums.Role;
import com.purohitcommunity.portal.repository.ActivityRepository;
import com.purohitcommunity.portal.repository.MembershipPlanRepository;
import com.purohitcommunity.portal.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("render")
class PostgresContainerSmokeTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("DB_HOST", POSTGRES::getHost);
        registry.add("DB_PORT", POSTGRES::getFirstMappedPort);
        registry.add("DB_NAME", POSTGRES::getDatabaseName);
        registry.add("DB_USERNAME", POSTGRES::getUsername);
        registry.add("DB_PASSWORD", POSTGRES::getPassword);
        registry.add("JWT_SECRET", () -> "integration-test-only-jwt-secret-with-at-least-32-bytes");
        registry.add("APP_ADMIN_EMAIL", () -> "admin@example.test");
        registry.add("APP_ADMIN_PASSWORD", () -> "Test-only-password-123!");
        registry.add("CORS_ALLOWED_ORIGINS", () -> "https://example.vercel.app");
        registry.add("PRIVATE_UPLOAD_DIR", () -> "./target/postgres-test-uploads");
    }

    @Autowired UserAccountRepository users;
    @Autowired MembershipPlanRepository plans;
    @Autowired ActivityRepository activities;
    @Autowired PasswordEncoder encoder;
    @Autowired DataInitializer initializer;

    @Test
    void bootstrapsPostgresAndPreservesAdminAndPlansOnRestart() {
        var admin = users.findByEmailIgnoreCase("admin@example.test").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(encoder.matches("Test-only-password-123!", admin.getPasswordHash()));
        assertEquals("ప్రీమియం సభ్యత్వం", plans.findByCode(PlanCode.PREMIUM).orElseThrow().getNameTelugu());
        assertTrue(plans.findByCode(PlanCode.ELITE).isPresent());
        assertEquals(0, activities.count(), "Hosted deployments must not create demo activities");

        String originalHash = admin.getPasswordHash();
        initializer.run();
        assertEquals(1, users.count());
        assertEquals(2, plans.count());
        assertEquals(originalHash, users.findByEmailIgnoreCase("admin@example.test").orElseThrow().getPasswordHash());
    }
}
