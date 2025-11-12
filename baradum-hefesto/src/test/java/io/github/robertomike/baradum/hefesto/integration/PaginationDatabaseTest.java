package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
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
 * Integration tests for pagination functionality with real database queries.
 * Tests verify that .page() correctly paginates results.
 */
@ExtendWith(DatabaseExtension.class)
class PaginationDatabaseTest {

    @Test
    void testPageWithDefaultSize() {
        var page = Baradum.make(User.class)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(15, page.getContent().size(), "Should return all 15 users with default page size");
    }

    @Test
    void testPageWithCustomSize() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "5");
        
        var page = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(5, page.getContent().size(), "Should return 5 users per page");
    }

    @Test
    void testPageWithOffset() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "5");
        params.put("offset", "5");
        
        var page = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(5, page.getContent().size(), "Should return 5 users from offset 5");
    }

    @Test
    void testPageWithOffsetBeyondResults() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "5");
        params.put("offset", "20");
        
        var page = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertTrue(page.getContent().isEmpty(), "Should return empty list when offset > total");
    }

    @Test
    void testPageWithFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("limit", "3");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new io.github.robertomike.baradum.core.filters.ExactFilter("country"))
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(3, page.getContent().size(), "Should return 3 USA users");
        page.getContent().forEach((User user) -> assertEquals("USA", user.getCountry()));
    }

    @Test
    void testPageWithFilterAndSort() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("sort", "-salary");
        params.put("limit", "3");
        
        var page = Baradum.make(User.class)
                .allowedFilters(new io.github.robertomike.baradum.core.filters.ExactFilter("country"))
                .allowedSort("salary")
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(3, page.getContent().size(), "Should return 3 USA users");
        
        // Verify all from USA
        page.getContent().forEach((User user) -> assertEquals("USA", user.getCountry()));
        
        // Verify sorted by salary descending
        List<User> users = page.getContent();
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() >= users.get(i).getSalary(),
                "Users should be sorted by salary descending");
        }
    }

    @Test
    void testPageIteration() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "5");
        
        // First page
        params.put("offset", "0");
        var page1 = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page1);
        assertEquals(5, page1.getContent().size());
        
        // Second page
        params.put("offset", "5");
        var page2 = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page2);
        assertEquals(5, page2.getContent().size());
        
        // Third page
        params.put("offset", "10");
        var page3 = Baradum.make(User.class)
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page3);
        assertEquals(5, page3.getContent().size());
        
        // Verify no overlap
        assertNotEquals(page1.getContent().get(0).getId(), page2.getContent().get(0).getId());
        assertNotEquals(page2.getContent().get(0).getId(), page3.getContent().get(0).getId());
    }

    @Test
    void testPageWithSortingAndPagination() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "age");
        params.put("limit", "10");
        
        var page = Baradum.make(User.class)
                .allowedSort("age")
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertEquals(10, page.getContent().size());
        
        // Verify sorted by age
        List<User> users = page.getContent();
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getAge() <= users.get(i).getAge(),
                "Users should be sorted by age ascending");
        }
    }

    @Test
    void testPageWithComplexFiltering() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        params.put("age", "25,35");
        params.put("country", "USA");
        params.put("sort", "-salary");
        params.put("limit", "5");
        
        var page = Baradum.make(User.class)
                .allowedFilters(
                        new io.github.robertomike.baradum.core.filters.EnumFilter("status", 
                            io.github.robertomike.baradum.hefesto.models.Status.class),
                        new io.github.robertomike.baradum.core.filters.IntervalFilter("age"),
                        new io.github.robertomike.baradum.core.filters.ExactFilter("country"))
                .allowedSort("salary")
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find matching users");
        assertTrue(page.getContent().size() <= 5, "Should not exceed page size");
        
        // Verify all filters applied
        List<User> pageUsers = page.getContent();
        pageUsers.forEach(user -> {
            assertEquals(io.github.robertomike.baradum.hefesto.models.Status.ACTIVE, user.getStatus());
            assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
            assertEquals("USA", user.getCountry());
        });
    }
}






