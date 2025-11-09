package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
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
 * Integration tests for sorting functionality with real database queries.
 * Tests verify that sorting works correctly using .get() and .page() methods.
 */
@ExtendWith(DatabaseExtension.class)
class SortingDatabaseTest {

    @Test
    void testSortByAgeAscending() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "age");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("age")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size(), "Should retrieve all users");
        
        // Verify ascending order
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getAge() <= users.get(i).getAge(),
                "Users should be sorted by age ascending");
        }
    }

    @Test
    void testSortByAgeDescending() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "-age");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("age")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size());
        
        // Verify descending order
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getAge() >= users.get(i).getAge(),
                "Users should be sorted by age descending");
        }
    }

    @Test
    void testSortBySalaryAscending() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "salary");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("salary")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size());
        
        // Verify ascending order
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() <= users.get(i).getSalary(),
                "Users should be sorted by salary ascending");
        }
    }

    @Test
    void testSortBySalaryDescending() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "-salary");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("salary")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size());
        
        // Verify descending order
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() >= users.get(i).getSalary(),
                "Users should be sorted by salary descending");
        }
    }

    @Test
    void testSortByUsername() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "username");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("username")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size());
        
        // Verify alphabetical order
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getUsername().compareTo(users.get(i).getUsername()) <= 0,
                "Users should be sorted by username alphabetically");
        }
    }

    @Test
    void testSortByCountryAndAge() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "country,-age");
        
        List<User> users = Baradum.make(User.class)
                .allowedSort("country", "age")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertEquals(15, users.size());
        
        // Verify multi-level sorting: country ascending, then age descending
        for (int i = 1; i < users.size(); i++) {
            String prevCountry = users.get(i - 1).getCountry();
            String currCountry = users.get(i).getCountry();
            
            if (prevCountry.equals(currCountry)) {
                // Same country, age should be descending
                assertTrue(users.get(i - 1).getAge() >= users.get(i).getAge(),
                    "Within same country, users should be sorted by age descending");
            } else {
                // Different country, should be alphabetically ascending
                assertTrue(prevCountry.compareTo(currCountry) <= 0,
                    "Countries should be sorted alphabetically");
            }
        }
    }

    @Test
    void testSortUsingPage() {
        Map<String, String> params = new HashMap<>();
        params.put("sort", "-salary");
        
        var page = Baradum.make(User.class)
                .allowedSort("salary")
                .withParams(params)
                .page(15);  // Default page size
        
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(15, page.getContent().size());
        
        // Verify descending order in page
        List<User> users = page.getContent();
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() >= users.get(i).getSalary(),
                "Users in page should be sorted by salary descending");
        }
    }

    @Test
    void testSortCombinedWithFilter() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("sort", "-salary");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(new io.github.robertomike.baradum.core.filters.ExactFilter("country"))
                .allowedSort("salary")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find USA users");
        users.forEach((User user) -> assertEquals("USA", user.getCountry()));
        
        // Verify sorting within filtered results
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() >= users.get(i).getSalary(),
                "Filtered users should be sorted by salary descending");
        }
    }

    @Test
    void testSortWithMultipleFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");
        params.put("age", "25,35");
        params.put("sort", "salary");
        
        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new io.github.robertomike.baradum.core.filters.EnumFilter("status", Status.class),
                        new io.github.robertomike.baradum.core.filters.IntervalFilter("age"))
                .allowedSort("salary")
                .withParams(params)
                .get();
        
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find ACTIVE users aged 25-35");
        
        // Verify all filters applied
        users.forEach((User user) -> {
            assertEquals(Status.ACTIVE, user.getStatus());
            assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
        });
        
        // Verify sorting
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getSalary() <= users.get(i).getSalary(),
                "Filtered users should be sorted by salary ascending");
        }
    }
}






