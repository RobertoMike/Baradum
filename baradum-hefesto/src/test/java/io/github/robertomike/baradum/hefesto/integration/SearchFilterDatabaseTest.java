package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.filters.SearchFilter;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
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
 * Integration tests for SearchFilter with real database queries.
 * Tests verify that SearchFilter correctly searches across multiple fields using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class SearchFilterDatabaseTest {

    @Test
    void testSearchFilterAcrossMultipleFields() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "john");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find 1 user with 'john' in username, email, or fullName");
        User user = users.get(0);
        assertTrue(user.getUsername().contains("john") || 
                   user.getEmail().contains("john") || 
                   user.getFullName().contains("John"));
    }

    @Test
    void testSearchFilterInUsername() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "alice");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find Alice");
        assertEquals("alicebrown", users.get(0).getUsername());
    }

    @Test
    void testSearchFilterInEmail() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "miller");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find user with 'miller' in email or name");
        assertTrue(users.get(0).getEmail().contains("miller") || 
                   users.get(0).getFullName().contains("Miller"));
    }

    @Test
    void testSearchFilterInFullName() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "Taylor");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find Grace Taylor");
        assertEquals("Grace Taylor", users.get(0).getFullName());
    }

    @Test
    void testSearchFilterWithCommonTerm() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "example");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("email"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size(), "All users have example.com in email");
    }

    @Test
    void testSearchFilterUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "son");
        
        var page = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find users with 'son' in their details");
        // Should find users like bobwilson, henryanderson, jackjackson
        assertTrue(page.getContent().size() >= 3);
    }

    @Test
    void testSearchFilterWithCustomParamName() {
        Map<String, String> params = new HashMap<>();
        params.put("q", "garcia");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new SearchFilter("q", "username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(1, users.size(), "Should find Noah Garcia");
        assertEquals("noahgarcia", users.get(0).getUsername());
    }

    @Test
    void testSearchFilterWithNoResults() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "zzz999");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("username", "email", "fullName"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should not find users with zzz999");
    }

    @Test
    void testSearchFilterCombinedWithExactFilter() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "example");
        params.put("country", "USA");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        SearchFilter.of("email"),
                        new io.github.robertomike.baradum.core.filters.ExactFilter("country"))
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find USA users with example.com email");
        users.forEach((User user) -> {
            assertEquals("USA", user.getCountry());
            assertTrue(user.getEmail().contains("example"));
        });
    }
}






