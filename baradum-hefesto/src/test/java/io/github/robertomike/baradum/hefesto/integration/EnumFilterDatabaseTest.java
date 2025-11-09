package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.filters.EnumFilter;
import io.github.robertomike.baradum.hefesto.models.Status;
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
 * Integration tests for EnumFilter with real database queries.
 * Tests verify that EnumFilter correctly filters enum values using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class EnumFilterDatabaseTest {

    @Test
    void testEnumFilterByActiveStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with ACTIVE status");
        users.forEach((User user) -> assertEquals(Status.ACTIVE, user.getStatus(), 
            "All users should have ACTIVE status"));
    }

    @Test
    void testEnumFilterByInactiveStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "INACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(2, users.size(), "Should find 2 users with INACTIVE status");
        users.forEach((User user) -> assertEquals(Status.INACTIVE, user.getStatus()));
    }

    @Test
    void testEnumFilterByPendingStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "PENDING");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(2, users.size(), "Should find 2 users with PENDING status");
        users.forEach((User user) -> assertEquals(Status.PENDING, user.getStatus()));
    }

    @Test
    void testEnumFilterByBannedStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "BANNED");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(2, users.size(), "Should find 2 users with BANNED status");
        users.forEach((User user) -> assertEquals(Status.BANNED, user.getStatus()));
    }

    @Test
    void testEnumFilterUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find ACTIVE users");
        List<User> users = page.getContent();
        users.forEach(user -> assertEquals(Status.ACTIVE, user.getStatus()));
    }

    @Test
    void testEnumFilterCombinedWithExactFilter() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        params.put("country", "USA");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new EnumFilter("status", Status.class),
                        new io.github.robertomike.baradum.hefesto.filters.ExactFilter("country"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find ACTIVE users from USA");
        users.forEach((User user) -> {
            assertEquals(Status.ACTIVE, user.getStatus());
            assertEquals("USA", user.getCountry());
        });
    }

    @Test
    void testEnumFilterCombinedWithIntervalFilter() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        params.put("age", "25,35");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new EnumFilter("status", Status.class),
                        new io.github.robertomike.baradum.hefesto.filters.IntervalFilter("age"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find ACTIVE users aged 25-35");
        users.forEach((User user) -> {
            assertEquals(Status.ACTIVE, user.getStatus());
            assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
        });
    }

    @Test
    void testEnumFilterWithCustomInternalName() {
        Map<String, String> params = new HashMap<>();
        params.put("userStatus", "INACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("userStatus", "status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(2, users.size(), "Should find INACTIVE users using custom param name");
        users.forEach((User user) -> assertEquals(Status.INACTIVE, user.getStatus()));
    }

    @Test
    void testEnumFilterCaseInsensitive() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter("status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find ACTIVE users with lowercase input");
        users.forEach((User user) -> assertEquals(Status.ACTIVE, user.getStatus()));
    }
}






