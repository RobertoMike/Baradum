package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.filters.ComparisonFilter;
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
 * Integration tests for ComparisonFilter with database operations.
 * Tests various comparison operators: >, >=, <, <=, !=, =
 */
@ExtendWith(DatabaseExtension.class)
class ComparisonFilterDatabaseTest {

    @Test
    void testComparisonFilterGreaterThan() {
        // Test: age > 30
        Map<String, String> params = new HashMap<>();
        params.put("age", ">30");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users older than 30");
        assertTrue(users.stream().allMatch(u -> u.getAge() > 30), 
                "All users should have age > 30");
    }

    @Test
    void testComparisonFilterGreaterOrEqual() {
        // Test: age >= 30
        Map<String, String> params = new HashMap<>();
        params.put("age", ">=30");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users 30 or older");
        assertTrue(users.stream().allMatch(u -> u.getAge() >= 30), 
                "All users should have age >= 30");
    }

    @Test
    void testComparisonFilterLessThan() {
        // Test: age < 30
        Map<String, String> params = new HashMap<>();
        params.put("age", "<30");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users younger than 30");
        assertTrue(users.stream().allMatch(u -> u.getAge() < 30), 
                "All users should have age < 30");
    }

    @Test
    void testComparisonFilterLessOrEqual() {
        // Test: age <= 25
        Map<String, String> params = new HashMap<>();
        params.put("age", "<=25");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users 25 or younger");
        assertTrue(users.stream().allMatch(u -> u.getAge() <= 25), 
                "All users should have age <= 25");
    }

    @Test
    void testComparisonFilterNotEqual() {
        // Test: age != 28
        Map<String, String> params = new HashMap<>();
        params.put("age", "!=28");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users not 28 years old");
        assertTrue(users.stream().allMatch(u -> u.getAge() != 28), 
                "All users should have age != 28");
    }

    @Test
    void testComparisonFilterEqual() {
        // Test: age = 28 (default when no operator prefix)
        Map<String, String> params = new HashMap<>();
        params.put("age", "28");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users exactly 28 years old");
        assertTrue(users.stream().allMatch(u -> u.getAge() == 28), 
                "All users should have age = 28");
    }

    @Test
    void testComparisonFilterOnSalary() {
        // Test: salary > 60000
        Map<String, String> params = new HashMap<>();
        params.put("salary", ">60000");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("salary"))
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find users with salary > 60000");
        assertTrue(users.stream().allMatch(u -> u.getSalary() > 60000), 
                "All users should have salary > 60000");
    }

    @Test
    void testComparisonFilterCombinedWithExactFilter() {
        // Test: age > 25 AND country = USA
        Map<String, String> params = new HashMap<>();
        params.put("age", ">25");
        params.put("country", "USA");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new ComparisonFilter("age"),
                        new ExactFilter("country")
                )
                .withParams(params)
                .get();

        assertFalse(users.isEmpty(), "Should find USA users older than 25");
        assertTrue(users.stream().allMatch(u -> u.getAge() > 25 && "USA".equals(u.getCountry())), 
                "All users should have age > 25 and be from USA");
    }

    @Test
    void testComparisonFilterUsingPage() {
        // Test: pagination with age >= 25
        Map<String, String> params = new HashMap<>();
        params.put("age", ">=25");

        var page = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .page(5, 0);

        assertNotNull(page, "Page should not be null");
        assertNotNull(page.getContent(), "Page content should not be null");
        assertTrue(page.getContent().stream().allMatch(u -> u.getAge() >= 25), 
                "All users in page should have age >= 25");
    }

    @Test
    void testComparisonFilterWithNoResults() {
        // Test: age > 200 (should find no users)
        Map<String, String> params = new HashMap<>();
        params.put("age", ">200");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ComparisonFilter("age"))
                .withParams(params)
                .get();

        assertTrue(users.isEmpty(), "Should find no users older than 200");
    }
}
