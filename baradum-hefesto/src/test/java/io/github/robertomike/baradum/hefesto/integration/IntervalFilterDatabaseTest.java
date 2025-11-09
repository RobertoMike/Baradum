package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.filters.IntervalFilter;
import io.github.robertomike.baradum.hefesto.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for IntervalFilter with real database queries.
 * Tests verify that IntervalFilter correctly filters numeric ranges using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class IntervalFilterDatabaseTest {

    @Test
    void testIntervalFilterByAgeRange() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "25,30");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users aged 25-30");
        users.forEach((User user) -> {
            assertTrue(user.getAge() >= 25 && user.getAge() <= 30, 
                "User age should be between 25 and 30: " + user.getAge());
        });
    }

    @Test
    void testIntervalFilterByAgeMinOnly() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "40,");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users aged 40+");
        users.forEach((User user) -> assertTrue(user.getAge() >= 40, 
            "User age should be >= 40: " + user.getAge()));
    }

    @Test
    void testIntervalFilterByAgeMaxOnly() {
        Map<String, String> params = new HashMap<>();
        params.put("age", ",27");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users aged <= 27");
        users.forEach((User user) -> assertTrue(user.getAge() <= 27, 
            "User age should be <= 27: " + user.getAge()));
    }

    @Test
    void testIntervalFilterBySalaryRange() {
        Map<String, String> params = new HashMap<>();
        params.put("salary", "70000,80000");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("salary"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with salary 70k-80k");
        users.forEach((User user) -> {
            assertTrue(user.getSalary() >= 70000 && user.getSalary() <= 80000,
                "User salary should be between 70000 and 80000: " + user.getSalary());
        });
    }

    @Test
    void testIntervalFilterBySalaryMinOnly() {
        Map<String, String> params = new HashMap<>();
        params.put("salary", "90000,");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("salary"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with salary >= 90k");
        users.forEach((User user) -> assertTrue(user.getSalary() >= 90000,
            "User salary should be >= 90000: " + user.getSalary()));
    }

    @Test
    void testIntervalFilterUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "30,40");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find users aged 30-40");
        page.getContent().forEach((User user) -> {
            assertTrue(user.getAge() >= 30 && user.getAge() <= 40,
                "User age should be between 30 and 40: " + user.getAge());
        });
    }

    @Test
    void testMultipleIntervalFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "25,35");
        params.put("salary", "70000,85000");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new IntervalFilter("age"),
                        new IntervalFilter("salary"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users matching both age and salary ranges");
        users.forEach((User user) -> {
            assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
            assertTrue(user.getSalary() >= 70000 && user.getSalary() <= 85000);
        });
    }

    @Test
    void testIntervalFilterWithNoResults() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "100,120");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should not find users aged 100-120");
    }

    @Test
    void testIntervalFilterWithCustomInternalName() {
        Map<String, String> params = new HashMap<>();
        params.put("years", "28,32");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("years", "age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users aged 28-32 using custom param name");
        users.forEach((User user) -> assertTrue(user.getAge() >= 28 && user.getAge() <= 32));
    }
}






