package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.filters.IsNullFilter;
import io.github.robertomike.baradum.core.filters.ExactFilter;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for IsNullFilter with database operations.
 * Tests NULL and NOT NULL conditions.
 */
@ExtendWith(DatabaseExtension.class)
class IsNullFilterDatabaseTest {

    @Test
    void testIsNullFilterForNullValues() {
        // Test: fullName IS NULL
        // Note: Test data has no null fullNames, so this should return empty list
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "null");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IsNullFilter("fullName"))
                .withParams(params)
                .get();

        // Since test data has no NULL fullName values, list should be empty
        assertTrue(users.isEmpty(), "Should find no users with null fullName in test data");
    }

    @Test
    void testIsNullFilterForNotNullValues() {
        // Test: fullName IS NOT NULL
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "not_null");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IsNullFilter("fullName"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users with not null fullName");
        assertTrue(users.stream().allMatch(u -> u.getFullName() != null), 
                "All users should have non-null fullName");
    }

    @Test
    void testIsNullFilterWithTrueValue() {
        // Test: using "true" for IS NULL
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "true");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IsNullFilter("fullName"))
                .withParams(params)
                .get();

        assertTrue(users.stream().allMatch(u -> u.getFullName() == null), 
                "All users should have null fullName");
    }

    @Test
    void testIsNullFilterWithFalseValue() {
        // Test: using "false" for IS NOT NULL
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "false");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IsNullFilter("fullName"))
                .withParams(params)
                .get();

        assertTrue(users.stream().allMatch(u -> u.getFullName() != null), 
                "All users should have non-null fullName");
    }

    @Test
    void testIsNullFilterCombinedWithOtherFilters() {
        // Test: fullName IS NULL AND country = USA
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "null");
        params.put("country", "USA");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new IsNullFilter("fullName"),
                        new ExactFilter("country")
                )
                .withParams(params)
                .get();

        assertTrue(users.stream().allMatch(u -> u.getFullName() == null && "USA".equals(u.getCountry())), 
                "All users should have null fullName and be from USA");
    }
}
