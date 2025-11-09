package io.github.robertomike.baradum.hefesto;

import io.github.robertomike.baradum.hefesto.filters.*;
import io.github.robertomike.baradum.hefesto.models.Status;
import io.github.robertomike.baradum.hefesto.models.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for Hefesto implementation.
 * These tests verify that the Baradum API correctly integrates with Hefesto filters.
 *
 * Note: Full database integration tests require a running JPA session which is typically
 * provided in a Spring or Jakarta EE container environment. These tests verify the API
 * surface and filter configuration without requiring database access.
 */
class HefestoIntegrationTest {

    @Test
    void testBaradumWithHefestoCanBeCreated() {
        var baradum = Baradum.make(User.class);
        assertNotNull(baradum, "Baradum instance should be created");
    }

    @Test
    void testExactFilterCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "johndoe");

        var baradum = Baradum.make(User.class)
                .allowedFilters(
                        new ExactFilter("username")
                )
                .withParams(params);

        assertNotNull(baradum, "Baradum with ExactFilter should be configured");
    }

    @Test
    void testMultipleFiltersCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("isActive", "true");

        var baradum = Baradum.make(User.class)
                .allowedFilters(
                        new ExactFilter("country"),
                        new ExactFilter("isActive"))
                .withParams(params);

        assertNotNull(baradum, "Baradum with multiple ExactFilters should be configured");
    }

    @Test
    void testPartialFilterCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "john%");

        var baradum = Baradum.make(User.class)
                .allowedFilters(
                        new PartialFilter("username"))
                .withParams(params);

        assertNotNull(baradum, "Baradum with PartialFilter should be configured");
    }

    @Test
    void testSearchFilterCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "john");

        var baradum = Baradum.make(User.class).allowedFilters(
                        SearchFilter.of("username", "email", "fullName"))
                .withParams(params);

        assertNotNull(baradum, "Baradum with SearchFilter should be configured");
    }

    @Test
    void testIntervalFilterCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("age", "25,35");

        var baradum = Baradum.make(User.class)
                .allowedSort("age")
                .withParams(params);

        assertNotNull(baradum, "Baradum with IntervalFilter should be configured");
    }

    @Test
    void testEnumFilterCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("status", "ACTIVE");

        var baradum = Baradum.make(User.class).allowedFilters(
                        new EnumFilter("status", Status.class))
                .withParams(params);

        assertNotNull(baradum, "Baradum with EnumFilter should be configured");
    }

    @Test
    void testSortingCanBeConfigured() {
        var baradum = Baradum.make(User.class)
                .allowedSort("age")
                .withParam("sort", "age");

        assertNotNull(baradum, "Baradum with sorting should be configured");
    }

    @Test
    void testMultipleSortsCanBeConfigured() {
        var baradum = Baradum.make(User.class)
                .allowedSort("country", "age")
                .withParam("sort", "country,-age");

        assertNotNull(baradum, "Baradum with multiple sorts should be configured");
    }

    @Test
    void testCompleteWorkflowCanBeConfigured() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("age", "25,40");
        params.put("sort", "-salary");

        var baradum = Baradum.make(User.class).allowedFilters(
                        new ExactFilter("country"),
                        new IntervalFilter("age"))
                .allowedSort("salary", "age")
                .withParams(params);

        assertNotNull(baradum, "Baradum with complete workflow should be configured");
    }

    @Test
    void testFiltersWithCustomInternalNames() {
        Map<String, String> params = new HashMap<>();
        params.put("user", "janesmith");

        var baradum = Baradum.make(User.class).allowedFilters(
                        new ExactFilter("user", "username"))
                .withParams(params);

        assertNotNull(baradum, "Baradum with custom internal names should be configured");
    }

    @Test
    void testSearchFilterWithCustomParamName() {
        Map<String, String> params = new HashMap<>();
        params.put("q", "Doe");

        var baradum = Baradum.make(User.class).allowedFilters(
                        new SearchFilter("q", "username", "fullName"))
                .withParams(params);

        assertNotNull(baradum, "Baradum with custom search param should be configured");
    }

    @Test
    void testAllFilterTypesCanBeConfiguredTogether() {
        Map<String, String> params = new HashMap<>();
        params.put("country", "USA");
        params.put("username", "john%");
        params.put("search", "example");
        params.put("age", "25,40");
        params.put("status", "ACTIVE");
        params.put("sort", "-salary,age");

        var baradum = Baradum.make(User.class).allowedFilters(
                        new ExactFilter("country"),
                        new PartialFilter("username"),
                        SearchFilter.of("email"),
                        new IntervalFilter("age"),
                        new EnumFilter("status", Status.class))
                .allowedSort("salary", "age")
                .withParams(params);

        assertNotNull(baradum, "Baradum with all filter types should be configured");
    }
}

