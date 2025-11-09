package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.filters.*;
import io.github.robertomike.baradum.hefesto.models.Status;
import io.github.robertomike.baradum.hefesto.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete integration tests that exercise full workflows with multiple filters,
 * sorting, and pagination using real database queries.
 */
@ExtendWith(DatabaseExtension.class)
class CompleteWorkflowDatabaseTest {

    @Test
    void testCompleteWorkflowWithAllFilterTypes() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");           // ExactFilter
        params.put("search", "example");        // SearchFilter
        params.put("age", "25,40");             // IntervalFilter
        params.put("status", "ACTIVE");         // EnumFilter
        params.put("sort", "-salary,age");      // Multi-level sorting
        params.put("limit", "5");               // Pagination

        var page = Baradum.make(User.class)
                .allowedFilters(
                        new ExactFilter("country"),
                        SearchFilter.of("email"),
                        new IntervalFilter("age"),
                        new EnumFilter("status", Status.class))
                .allowedSort("salary", "age")
                .withParams(params)
                .page(15);  // Default page size

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find users matching all criteria");
        assertTrue(page.getContent().size() <= 5, "Should respect page size limit");

        // Verify all filters applied
        List<User> pageUsers = page.getContent();
        pageUsers.forEach(user -> {
            assertEquals("USA", user.getCountry());
            assertTrue(user.getEmail().contains("example"));
            assertTrue(user.getAge() >= 25 && user.getAge() <= 40);
            assertEquals(Status.ACTIVE, user.getStatus());
        });

        // Verify sorting (salary descending, then age ascending)
        List<User> users = page.getContent();
        for (int i = 1; i < users.size(); i++) {
            double prevSalary = users.get(i - 1).getSalary();
            double currSalary = users.get(i).getSalary();

            if (prevSalary == currSalary) {
                // Same salary, age should be ascending
                assertTrue(users.get(i - 1).getAge() <= users.get(i).getAge(),
                        "Within same salary, users should be sorted by age ascending");
            } else {
                // Salary should be descending
                assertTrue(prevSalary >= currSalary,
                        "Users should be sorted by salary descending");
            }
        }
    }

    @Test
    void testWorkflowWithPartialAndExactFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "son");         // PartialFilter - ends with 'son'
        params.put("isActive", "true");         // ExactFilter
        params.put("sort", "age");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new PartialFilter("username").setStrategy(SearchLikeStrategy.START),
                        new ExactFilter("isActive"))
                .allowedSort("age")
                .withParams(params)
                .get();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find active users with 'son' in username");

        users.forEach((User user) -> {
            assertTrue(user.getUsername().endsWith("son") || user.getUsername().contains("son"));
            assertTrue(user.getIsActive());
        });

        // Verify sorted by age
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getAge() <= users.get(i).getAge());
        }
    }

    @Test
    void testWorkflowWithSearchAndEnumFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("q", "smith");               // SearchFilter with custom param
        params.put("status", "ACTIVE");         // EnumFilter

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new SearchFilter("q", "username", "email", "fullName").setStrategy(SearchLikeStrategy.COMPLETE),
                        new EnumFilter("status", Status.class))
                .withParams(params)
                .get();

        assertNotNull(users);
        assertEquals(1, users.size(), "Should find 1 ACTIVE user with 'smith'");

        User user = users.get(0);
        assertEquals(Status.ACTIVE, user.getStatus());
        assertTrue(user.getUsername().contains("smith") ||
                user.getEmail().contains("smith") ||
                user.getFullName().contains("Smith"));
    }

    @Test
    void testWorkflowWithMultipleIntervalFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "25,35");             // IntervalFilter
        params.put("salary", "70000,85000");    // IntervalFilter
        params.put("country", "USA");           // ExactFilter
        params.put("sort", "-age");
        params.put("limit", "10");

        var page = Baradum.make(User.class)
                .allowedFilters(
                        new IntervalFilter("age"),
                        new IntervalFilter("salary"),
                        new ExactFilter("country"))
                .allowedSort("age")
                .withParams(params)
                .page(15);  // Default page size

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertFalse(page.getContent().isEmpty(), "Should find users matching all criteria");

        page.getContent().forEach((User user) -> {
            assertTrue(user.getAge() >= 25 && user.getAge() <= 35);
            assertTrue(user.getSalary() >= 70000 && user.getSalary() <= 85000);
            assertEquals("USA", user.getCountry());
        });
    }

    @Test
    void testWorkflowWithCustomInternalNames() {
        Map<String, String> params = new HashMap<>();
        params.put("user", "john");             // ExactFilter with custom name
        params.put("years", "28,32");           // IntervalFilter with custom name
        params.put("userStatus", "ACTIVE");     // EnumFilter with custom name

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new PartialFilter("user", "username"),
                        new IntervalFilter("years", "age"),
                        new EnumFilter("userStatus", "status", Status.class))
                .withParams(params)
                .get();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with custom param names");

        users.forEach((User user) -> {
            assertTrue(user.getUsername().contains("john"));
            assertTrue(user.getAge() >= 28 && user.getAge() <= 32);
            assertEquals(Status.ACTIVE, user.getStatus());
        });
    }

    @Test
    void testWorkflowFindingSpecificUser() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "janesmith");

        var userOpt = Baradum.make(User.class)
                .allowedFilters(new ExactFilter("username"))
                .withParams(params)
                .findFirst();

        assertTrue(userOpt.isPresent(), "Should find Jane Smith");
        User user = userOpt.get();
        assertEquals("janesmith", user.getUsername());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertEquals("Jane Smith", user.getFullName());
        assertEquals(28, user.getAge());
        assertEquals(Status.ACTIVE, user.getStatus());
    }

    @Test
    void testWorkflowWithEmptyResultSet() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "Antarctica");
        params.put("age", "100,120");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(
                        new ExactFilter("country"),
                        new IntervalFilter("age"))
                .withParams(params)
                .get();

        assertNotNull(users);
        assertTrue(users.isEmpty(), "Should return empty list for impossible criteria");
    }

    @Test
    void testWorkflowWithOnlyPaginationNoFilters() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", "7");
        params.put("offset", "5");
        params.put("sort", "username");

        var page = Baradum.make(User.class)
                .allowedSort("username")
                .withParams(params)
                .page(15);  // Default page size

        assertNotNull(page);
        assertEquals(7, page.getContent().size(), "Should return 7 users");

        // Verify alphabetical sorting
        List<User> users = page.getContent();
        for (int i = 1; i < users.size(); i++) {
            assertTrue(users.get(i - 1).getUsername().compareTo(users.get(i).getUsername()) <= 0,
                    "Users should be sorted alphabetically by username");
        }
    }

    @Test
    void testWorkflowGetAllUsersWithoutParameters() {
        List<User> users = Baradum.make(User.class)
                .get();

        assertNotNull(users);
        assertEquals(15, users.size(), "Should return all 15 users");
    }

    @Test
    void testWorkflowPageAllUsersWithoutFilters() {
        var page = Baradum.make(User.class)
                .page(15);  // Default page size

        assertNotNull(page);
        assertNotNull(page.getContent());
        assertEquals(15, page.getContent().size(), "Should return all 15 users");
    }
}






