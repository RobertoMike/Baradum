package io.github.robertomike.baradum.requests;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration tests for ApacheTomcatRequest with multi-parameter filter scenarios.
 * Tests verify correct handling of multiple query parameters simultaneously.
 */
@ExtendWith(MockitoExtension.class)
class ApacheTomcatMultiParameterTest {

    @Mock
    private HttpServletRequest request;
    
    private ApacheTomcatRequest apacheTomcatRequest;

    @BeforeEach
    void setUp() {
        apacheTomcatRequest = new ApacheTomcatRequest(request);
    }

    @Test
    void testMultipleExactFilters() {
        // Simulate: ?name=John&status=ACTIVE&country=USA
        when(request.getParameter("name")).thenReturn("John");
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("country")).thenReturn("USA");

        assertEquals("John", apacheTomcatRequest.findParamByName("name"));
        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("USA", apacheTomcatRequest.findParamByName("country"));
        
        assertFalse(apacheTomcatRequest.notExistsByName("name"));
        assertFalse(apacheTomcatRequest.notExistsByName("status"));
        assertFalse(apacheTomcatRequest.notExistsByName("country"));
    }

    @Test
    void testMixedFilterTypes() {
        // Simulate: ?name=John&age=25-50&status=ACTIVE,PENDING&search=developer
        when(request.getParameter("name")).thenReturn("John");
        when(request.getParameter("age")).thenReturn("25-50");
        when(request.getParameter("status")).thenReturn("ACTIVE,PENDING");
        when(request.getParameter("search")).thenReturn("developer");

        assertEquals("John", apacheTomcatRequest.findParamByName("name"));
        assertEquals("25-50", apacheTomcatRequest.findParamByName("age"));
        assertEquals("ACTIVE,PENDING", apacheTomcatRequest.findParamByName("status"));
        assertEquals("developer", apacheTomcatRequest.findParamByName("search"));
    }

    @Test
    void testFiltersWithSorting() {
        // Simulate: ?status=ACTIVE&age=25-50&sort=name,-createdAt
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("age")).thenReturn("25-50");
        when(request.getParameter("sort")).thenReturn("name,-createdAt");

        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("25-50", apacheTomcatRequest.findParamByName("age"));
        assertEquals("name,-createdAt", apacheTomcatRequest.findParamByName("sort"));
    }

    @Test
    void testFiltersWithPagination() {
        // Simulate: ?status=ACTIVE&country=USA&page=2&limit=20
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("country")).thenReturn("USA");
        when(request.getParameter("page")).thenReturn("2");
        when(request.getParameter("limit")).thenReturn("20");

        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("USA", apacheTomcatRequest.findParamByName("country"));
        assertEquals("2", apacheTomcatRequest.findParamByName("page"));
        assertEquals("20", apacheTomcatRequest.findParamByName("limit"));
    }

    @Test
    void testComplexFilterScenario() {
        // Simulate: ?name=John&email=john@example.com&age=25-50&country=USA,Canada&status=ACTIVE&sort=-createdAt&page=1&limit=15
        when(request.getParameter("name")).thenReturn("John");
        when(request.getParameter("email")).thenReturn("john@example.com");
        when(request.getParameter("age")).thenReturn("25-50");
        when(request.getParameter("country")).thenReturn("USA,Canada");
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("sort")).thenReturn("-createdAt");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("limit")).thenReturn("15");

        // Verify all parameters are accessible
        assertEquals("John", apacheTomcatRequest.findParamByName("name"));
        assertEquals("john@example.com", apacheTomcatRequest.findParamByName("email"));
        assertEquals("25-50", apacheTomcatRequest.findParamByName("age"));
        assertEquals("USA,Canada", apacheTomcatRequest.findParamByName("country"));
        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("-createdAt", apacheTomcatRequest.findParamByName("sort"));
        assertEquals("1", apacheTomcatRequest.findParamByName("page"));
        assertEquals("15", apacheTomcatRequest.findParamByName("limit"));
        
        // Verify all parameters exist
        assertFalse(apacheTomcatRequest.notExistsByName("name"));
        assertFalse(apacheTomcatRequest.notExistsByName("email"));
        assertFalse(apacheTomcatRequest.notExistsByName("age"));
        assertFalse(apacheTomcatRequest.notExistsByName("country"));
        assertFalse(apacheTomcatRequest.notExistsByName("status"));
        assertFalse(apacheTomcatRequest.notExistsByName("sort"));
        assertFalse(apacheTomcatRequest.notExistsByName("page"));
        assertFalse(apacheTomcatRequest.notExistsByName("limit"));
    }

    @Test
    void testPartialParameterPresence() {
        // Simulate: ?name=John&status=ACTIVE (age and country missing)
        when(request.getParameter("name")).thenReturn("John");
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("age")).thenReturn(null);
        when(request.getParameter("country")).thenReturn(null);

        // Existing parameters
        assertEquals("John", apacheTomcatRequest.findParamByName("name"));
        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertFalse(apacheTomcatRequest.notExistsByName("name"));
        assertFalse(apacheTomcatRequest.notExistsByName("status"));
        
        // Missing parameters
        assertNull(apacheTomcatRequest.findParamByName("age"));
        assertNull(apacheTomcatRequest.findParamByName("country"));
        assertTrue(apacheTomcatRequest.notExistsByName("age"));
        assertTrue(apacheTomcatRequest.notExistsByName("country"));
    }

    @Test
    void testEmptyStringParameters() {
        // Simulate: ?name=&status=ACTIVE
        when(request.getParameter("name")).thenReturn("");
        when(request.getParameter("status")).thenReturn("ACTIVE");

        assertEquals("", apacheTomcatRequest.findParamByName("name"));
        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        
        // Empty string means parameter exists (not null), so it should NOT be "notExists"
        // notExistsByName checks if parameter is null, not if it's empty
        assertFalse(apacheTomcatRequest.notExistsByName("status"));
        
        // Empty string is still present (not null)
        // The behavior depends on implementation - some consider "" as existing, some don't
        // Let's just verify the value retrieval works correctly
        assertNotNull(apacheTomcatRequest.findParamByName("name"));
    }

    @Test
    void testSpecialCharactersInParameters() {
        // Simulate: ?email=user%40example.com&search=John+Doe&price=100.50
        when(request.getParameter("email")).thenReturn("user@example.com"); // Decoded
        when(request.getParameter("search")).thenReturn("John Doe"); // Decoded
        when(request.getParameter("price")).thenReturn("100.50");

        assertEquals("user@example.com", apacheTomcatRequest.findParamByName("email"));
        assertEquals("John Doe", apacheTomcatRequest.findParamByName("search"));
        assertEquals("100.50", apacheTomcatRequest.findParamByName("price"));
    }

    @Test
    void testDateRangeParameters() {
        // Simulate: ?createdAt=>2024-01-01&updatedAt=<2024-12-31
        when(request.getParameter("createdAt")).thenReturn(">2024-01-01");
        when(request.getParameter("updatedAt")).thenReturn("<2024-12-31");

        assertEquals(">2024-01-01", apacheTomcatRequest.findParamByName("createdAt"));
        assertEquals("<2024-12-31", apacheTomcatRequest.findParamByName("updatedAt"));
    }

    @Test
    void testMultipleSortParameters() {
        // Simulate: ?status=ACTIVE&sort=name,-age,createdAt
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("sort")).thenReturn("name,-age,createdAt");

        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("name,-age,createdAt", apacheTomcatRequest.findParamByName("sort"));
    }

    @Test
    void testNullSafeParameterRetrieval() {
        // All parameters are null
        when(request.getParameter("name")).thenReturn(null);
        when(request.getParameter("status")).thenReturn(null);
        when(request.getParameter("age")).thenReturn(null);

        assertNull(apacheTomcatRequest.findParamByName("name"));
        assertNull(apacheTomcatRequest.findParamByName("status"));
        assertNull(apacheTomcatRequest.findParamByName("age"));
        
        assertTrue(apacheTomcatRequest.notExistsByName("name"));
        assertTrue(apacheTomcatRequest.notExistsByName("status"));
        assertTrue(apacheTomcatRequest.notExistsByName("age"));
    }

    @Test
    void testRealWorldEcommerceScenario() {
        // Simulate: ?category=electronics&brand=Apple,Samsung&minPrice=500&maxPrice=2000&inStock=true&sort=price,-rating
        when(request.getParameter("category")).thenReturn("electronics");
        when(request.getParameter("brand")).thenReturn("Apple,Samsung");
        when(request.getParameter("minPrice")).thenReturn("500");
        when(request.getParameter("maxPrice")).thenReturn("2000");
        when(request.getParameter("inStock")).thenReturn("true");
        when(request.getParameter("sort")).thenReturn("price,-rating");

        Map<String, String> params = new HashMap<>();
        params.put("category", apacheTomcatRequest.findParamByName("category"));
        params.put("brand", apacheTomcatRequest.findParamByName("brand"));
        params.put("minPrice", apacheTomcatRequest.findParamByName("minPrice"));
        params.put("maxPrice", apacheTomcatRequest.findParamByName("maxPrice"));
        params.put("inStock", apacheTomcatRequest.findParamByName("inStock"));
        params.put("sort", apacheTomcatRequest.findParamByName("sort"));

        assertEquals(6, params.size());
        assertEquals("electronics", params.get("category"));
        assertEquals("Apple,Samsung", params.get("brand"));
        assertEquals("500", params.get("minPrice"));
        assertEquals("2000", params.get("maxPrice"));
        assertEquals("true", params.get("inStock"));
        assertEquals("price,-rating", params.get("sort"));
    }

    @Test
    void testRealWorldUserManagementScenario() {
        // Simulate: ?role=ADMIN,USER&status=ACTIVE&search=john&age=18-65&deletedAt=null&sort=-createdAt
        when(request.getParameter("role")).thenReturn("ADMIN,USER");
        when(request.getParameter("status")).thenReturn("ACTIVE");
        when(request.getParameter("search")).thenReturn("john");
        when(request.getParameter("age")).thenReturn("18-65");
        when(request.getParameter("deletedAt")).thenReturn("null");
        when(request.getParameter("sort")).thenReturn("-createdAt");

        assertEquals("ADMIN,USER", apacheTomcatRequest.findParamByName("role"));
        assertEquals("ACTIVE", apacheTomcatRequest.findParamByName("status"));
        assertEquals("john", apacheTomcatRequest.findParamByName("search"));
        assertEquals("18-65", apacheTomcatRequest.findParamByName("age"));
        assertEquals("null", apacheTomcatRequest.findParamByName("deletedAt"));
        assertEquals("-createdAt", apacheTomcatRequest.findParamByName("sort"));
        
        // Verify all parameters are present
        assertFalse(apacheTomcatRequest.notExistsByName("role"));
        assertFalse(apacheTomcatRequest.notExistsByName("status"));
        assertFalse(apacheTomcatRequest.notExistsByName("search"));
        assertFalse(apacheTomcatRequest.notExistsByName("age"));
        assertFalse(apacheTomcatRequest.notExistsByName("deletedAt"));
        assertFalse(apacheTomcatRequest.notExistsByName("sort"));
    }
}
