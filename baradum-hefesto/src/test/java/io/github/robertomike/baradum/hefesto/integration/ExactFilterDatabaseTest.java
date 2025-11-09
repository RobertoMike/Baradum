package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.filters.EnumFilter;
import io.github.robertomike.baradum.hefesto.filters.ExactFilter;
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
 * Integration tests for ExactFilter with real database queries.
 * Tests verify that ExactFilter correctly filters data using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class ExactFilterDatabaseTest {

    @Test
    void testExactFilterByCountryUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("country"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users from USA");
        assertTrue(users.size() >= 8, "Should find at least 8 USA users");
        users.forEach((User user) -> assertEquals("USA", user.getCountry(), "All users should be from USA"));
    }

    @Test
    void testExactFilterByUsernameUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "johndoe");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find exactly one user");
        assertEquals("johndoe", users.get(0).getUsername());
        assertEquals("john.doe@example.com", users.get(0).getEmail());
    }

    @Test
    void testExactFilterByIsActiveUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("isActive", "true");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("isActive"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find active users");
        users.forEach((User user) -> assertTrue(user.getIsActive(), "All users should be active"));
    }

    @Test
    void testExactFilterByStatusUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("status", "status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find active status users");
        users.forEach((User user) -> assertEquals(Status.ACTIVE, user.getStatus(), "All users should have ACTIVE status"));
    }

    @Test
    void testMultipleExactFiltersUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("isActive", "true");
        params.put("status", "ACTIVE");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new ExactFilter("country"),
                        new ExactFilter("isActive"),
                        new EnumFilter<>("status", "status", Status.class))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users matching all criteria");
        users.forEach((User user) -> {
            assertEquals("USA", user.getCountry());
            assertTrue(user.getIsActive());
            assertEquals(Status.ACTIVE, user.getStatus());
        });
    }

    @Test
    void testExactFilterWithNoResultsUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "Antarctica");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("country"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should not find users from Antarctica");
    }

    @Test
    void testExactFilterByCountryUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "Canada");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("country"))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find users from Canada");
        assertTrue(page.getContent().size() >= 3, "Should find at least 3 Canadian users");
        page.getContent().forEach((User user) -> assertEquals("Canada", user.getCountry()));
    }

    @Test
    void testExactFilterWithCustomInternalNameUsingGet() {
        Map<String, String> params = new HashMap<>();
        params.put("user", "janesmith");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("user", "username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("janesmith", users.get(0).getUsername());
    }
}






