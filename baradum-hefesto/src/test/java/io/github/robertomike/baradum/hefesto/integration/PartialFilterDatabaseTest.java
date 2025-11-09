package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.filters.PartialFilter;
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
 * Integration tests for PartialFilter with real database queries.
 * Tests verify that PartialFilter correctly filters data using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class PartialFilterDatabaseTest {

    @Test
    void testPartialFilterByUsernameStartsWith() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "john%");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find 1 user starting with 'john'");
        assertTrue(users.get(0).getUsername().startsWith("john"));
    }

    @Test
    void testPartialFilterByUsernameEndsWith() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "%smith");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find 1 user ending with 'smith'");
        assertTrue(users.get(0).getUsername().endsWith("smith"));
    }

    @Test
    void testPartialFilterByUsernameContains() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "%a%");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with 'a' in username");
        users.forEach((User user) -> assertTrue(user.getUsername().contains("a"), 
            "Username should contain 'a': " + user.getUsername()));
    }

    @Test
    void testPartialFilterByEmailDomain() {
        Map<String, String> params = new HashMap<>();
        params.put("email", "%@example.com");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("email"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size(), "All users should have @example.com email");
        users.forEach((User user) -> assertTrue(user.getEmail().endsWith("@example.com")));
    }

    @Test
    void testPartialFilterByFullNameUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "%Miller%");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("fullName"))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(1, page.getContent().size(), "Should find 1 user with 'Miller' in full name");
        assertTrue(page.getContent().get(0).getFullName().contains("Miller"));
    }

    @Test
    void testMultiplePartialFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "jane%");
        params.put("email", "%smith%");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new PartialFilter("username"),
                        new PartialFilter("email"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find exactly 1 user matching both filters");
        assertTrue(users.get(0).getUsername().startsWith("jane"));
        assertTrue(users.get(0).getEmail().contains("smith"));
    }

    @Test
    void testPartialFilterWithNoResults() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "%xyz123%");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should not find users with xyz123 in username");
    }

    @Test
    void testPartialFilterCaseInsensitive() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "%JOHN%");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("username"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        // H2 with MODE=MySQL should handle case-insensitive comparison
        assertFalse(users.isEmpty(), "Should find users (case-insensitive)");
    }
}






