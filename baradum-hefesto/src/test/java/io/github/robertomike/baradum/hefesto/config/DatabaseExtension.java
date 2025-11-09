package io.github.robertomike.baradum.hefesto.config;

import io.github.robertomike.hefesto.builders.BaseBuilder;
import io.github.robertomike.hefesto.configs.HefestoAutoconfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JUnit 5 extension that manages the database lifecycle for tests.
 * Creates an H2 in-memory database before tests and cleans up after.
 * Loads test data from test-data.sql file.
 */
public class DatabaseExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private static Boolean isTestDataLoaded = false;
    
    @Override
    public void beforeAll(ExtensionContext context) {
        if (entityManagerFactory == null) {
            Map<String, String> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;IGNORECASE=TRUE");
            properties.put("jakarta.persistence.jdbc.user", "sa");
            properties.put("jakarta.persistence.jdbc.password", "");
            properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.put("hibernate.hbm2ddl.auto", "create-drop");
            properties.put("hibernate.show_sql", "false");
            properties.put("hibernate.format_sql", "true");
            
            entityManagerFactory = Persistence.createEntityManagerFactory("test-unit", properties);
        }

        // Load test data from SQL file
        entityManager = entityManagerFactory.createEntityManager();
        if (!isTestDataLoaded) {
            loadTestData();
            isTestDataLoaded = true;
        }
    }
    
    @Override
    public void beforeEach(ExtensionContext context) {
        new HefestoAutoconfiguration(entityManager);
        context.getStore(ExtensionContext.Namespace.GLOBAL).put("entityManager", entityManager);
    }
    
    private void loadTestData() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test-data.sql")) {
            if (is == null) {
                throw new RuntimeException("test-data.sql not found in classpath");
            }
            
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"))
                    .replace("\n", "");
            
            entityManager.getTransaction().begin();
            
            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty()) {
                    entityManager.createNativeQuery(statement).executeUpdate();
                }
            }
            
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to load test data", e);
        }
    }
    
    @Override
    public void afterEach(ExtensionContext context) {
    }
    
    @Override
    public void afterAll(ExtensionContext context) {
        // Don't close the factory as other tests might need it
        if (entityManager != null && entityManager.isOpen()) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
    
    public static EntityManager getEntityManager(ExtensionContext context) {
        return (EntityManager) context.getStore(ExtensionContext.Namespace.GLOBAL).get("entityManager");
    }
}
