package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.filters.InFilter;
import io.github.robertomike.baradum.core.filters.ComparisonFilter;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InFilter with database operations.
 * Tests IN operator with comma-separated values.
 */
@ExtendWith(DatabaseExtension.class)
class InFilterDatabaseTest {

    @Test
    void testInFilterWithMultipleCountries() {
        // Test: country IN ('USA', 'UK', 'Canada')
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA,UK,Canada");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new InFilter("country"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users from USA, UK, or Canada");
        assertTrue(users.stream().allMatch(u -> 
                "USA".equals(u.getCountry()) || "UK".equals(u.getCountry()) || "Canada".equals(u.getCountry())), 
                "All users should be from USA, UK, or Canada");
    }

    @Test
    void testInFilterWithSingleValue() {
        // Test: country IN ('USA') - should work like EQUAL
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new InFilter("country"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users from USA");
        assertTrue(users.stream().allMatch(u -> "USA".equals(u.getCountry())), 
                "All users should be from USA");
    }

    @Test
    void testInFilterWithSpacesInValues() {
        // Test: handles spaces around commas
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA , UK , Canada");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new InFilter("country"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users despite spaces in values");
        assertTrue(users.stream().allMatch(u -> 
                "USA".equals(u.getCountry()) || "UK".equals(u.getCountry()) || "Canada".equals(u.getCountry())), 
                "All users should be from USA, UK, or Canada");
    }

    @Test
    void testInFilterCombinedWithOtherFilters() {
        // Test: country IN ('USA', 'UK') AND age > 25
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA,UK");
        params.put("age", ">25");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new InFilter("country"),
                        new ComparisonFilter("age")
                )
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users from USA or UK older than 25");
        assertTrue(users.stream().allMatch(u -> 
                ("USA".equals(u.getCountry()) || "UK".equals(u.getCountry())) && u.getAge() > 25), 
                "All users should be from USA or UK and age > 25");
    }

    @Test
    void testInFilterUsingPage() {
        // Test: pagination with IN filter
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA,UK,Canada");

        var page = Baradum.make(User.class)
                .allowedFilters(new InFilter("country"))
                .withParams(params)
                .page(3, 0);

        assertNotNull(page, "Page should not be null");
        assertNotNull(page.getContent(), "Page content should not be null");
        assertTrue(page.getContent().stream().allMatch(u -> 
                "USA".equals(u.getCountry()) || "UK".equals(u.getCountry()) || "Canada".equals(u.getCountry())), 
                "All users in page should be from USA, UK, or Canada");
    }

    @Test
    void testInFilterWithNoResults() {
        // Test: country IN ('Mars', 'Jupiter') - should find no users
        Map<String, String> params = new HashMap<>();
        params.put("country", "Mars,Jupiter");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new InFilter("country"))
                .withParams(params)
                .get();

        assertTrue(users.isEmpty(), "Should find no users from Mars or Jupiter");
    }
}
