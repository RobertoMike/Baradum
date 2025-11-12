package io.github.robertomike.baradum.core

import io.github.robertomike.baradum.core.exceptions.BaradumException
import io.github.robertomike.baradum.core.filters.ExactFilter
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.sorting.OrderBy
import io.github.robertomike.baradum.core.enums.SortDirection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional

/**
 * Comprehensive tests for Baradum core class
 */
class BaradumTest {

    private lateinit var mockQueryBuilder: QueryBuilder<TestEntity>
    private lateinit var baradum: Baradum<TestEntity, QueryBuilder<TestEntity>>

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        baradum = Baradum(mockQueryBuilder)
        
        // Reset global state
        Baradum.request = null
    }

    // Constructor Tests
    @Test
    fun `constructor creates Baradum instance with query builder`() {
        assertNotNull(baradum)
    }

    // withParams Tests
    @Test
    fun `withParams sets instance parameters`() {
        val params = mapOf("name" to "test", "status" to "active")
        
        val result = baradum.withParams(params)
        
        assertSame(baradum, result, "Should return same instance for chaining")
    }

    @Test
    fun `withParams with empty map works`() {
        val result = baradum.withParams(emptyMap())
        assertSame(baradum, result)
    }

    @Test
    fun `withParam adds single parameter`() {
        val result = baradum.withParam("key", "value")
        assertSame(baradum, result)
    }

    @Test
    fun `withParam can add multiple parameters`() {
        baradum.withParam("key1", "value1")
               .withParam("key2", "value2")
               .withParam("key3", "value3")
        
        assertNotNull(baradum)
    }

    @Test
    fun `withParam overrides existing parameter`() {
        baradum.withParam("key", "value1")
               .withParam("key", "value2")
        
        assertNotNull(baradum)
    }

    // allowedFilters Tests
    @Test
    fun `allowedFilters with varargs adds filters`() {
        val filter1 = ExactFilter("name")
        val filter2 = ExactFilter("status")
        
        val result = baradum.allowedFilters(filter1, filter2)
        
        assertSame(baradum, result)
    }

    @Test
    fun `allowedFilters with list adds filters`() {
        val filters = listOf(
            ExactFilter("name"),
            ExactFilter("status")
        )
        
        val result = baradum.allowedFilters(filters)
        
        assertSame(baradum, result)
    }

    @Test
    fun `allowedFilters with empty varargs works`() {
        val result = baradum.allowedFilters()
        assertSame(baradum, result)
    }

    @Test
    fun `allowedFilters with empty list works`() {
        val result = baradum.allowedFilters(emptyList())
        assertSame(baradum, result)
    }

    // allowedSort Tests
    @Test
    fun `allowedSort with string varargs adds sorts`() {
        val result = baradum.allowedSort("name", "createdAt", "status")
        assertSame(baradum, result)
    }

    @Test
    fun `allowedSort with OrderBy varargs adds sorts`() {
        val result = baradum.allowedSort(
            OrderBy("name"),
            OrderBy("createdAt")
        )
        assertSame(baradum, result)
    }

    @Test
    fun `allowedSort with OrderBy list adds sorts`() {
        val sorts = listOf(
            OrderBy("name"),
            OrderBy("createdAt")
        )
        
        val result = baradum.allowedSort(sorts)
        assertSame(baradum, result)
    }

    @Test
    fun `allowedSort with empty varargs works`() {
        val emptyArray = emptyArray<String>()
        val result = baradum.allowedSort(*emptyArray)
        assertSame(baradum, result)
    }

    // selects Tests
    @Test
    fun `selects sets select fields on query builder`() {
        baradum.selects("id", "name", "email")
        
        verify(mockQueryBuilder).select("id", "name", "email")
    }

    @Test
    fun `selects returns same instance for chaining`() {
        val result = baradum.selects("id", "name")
        assertSame(baradum, result)
    }

    @Test
    fun `selects with empty varargs works`() {
        val result = baradum.selects()
        assertSame(baradum, result)
        verify(mockQueryBuilder).select()
    }

    // addSelects Tests
    @Test
    fun `addSelects adds select fields to query builder`() {
        baradum.addSelects("phone", "address")
        
        verify(mockQueryBuilder).addSelect("phone", "address")
    }

    @Test
    fun `addSelects returns same instance for chaining`() {
        val result = baradum.addSelects("phone")
        assertSame(baradum, result)
    }

    // useBody Tests
    @Test
    fun `useBody enables body mode`() {
        val result = baradum.useBody()
        assertSame(baradum, result)
    }

    @Test
    fun `useBody can be chained with other methods`() {
        val result = baradum.useBody()
                           .allowedFilters(ExactFilter("name"))
                           .allowedSort("createdAt")
        
        assertSame(baradum, result)
    }

    // useOnlyBody Tests
    @Test
    fun `useOnlyBody enables only body mode`() {
        val result = baradum.useOnlyBody()
        assertSame(baradum, result)
    }

    @Test
    fun `useOnlyBody throws exception when request is not POST`() {
        val mockRequest: BasicRequest<Any> = mock {
            on { isPost() } doReturn false
        }
        
        Baradum.request = mockRequest
        baradum.useOnlyBody()
        
        assertThrows<BaradumException> {
            baradum.get()
        }
    }

    // get Tests
    @Test
    fun `get calls query builder get method`() {
        val expected = listOf(TestEntity("1"), TestEntity("2"))
        whenever(mockQueryBuilder.get()).thenReturn(expected)
        
        val result = baradum.get()
        
        assertEquals(expected, result)
        verify(mockQueryBuilder).get()
    }

    @Test
    fun `get with instance params applies params`() {
        val params = mapOf("name" to "test")
        whenever(mockQueryBuilder.get()).thenReturn(emptyList())
        
        baradum.withParams(params)
               .allowedFilters(ExactFilter("name"))
               .get()
        
        verify(mockQueryBuilder).get()
    }

    // page Tests
    @Test
    fun `page with limit and offset calls query builder`() {
        val expected = Page(listOf(TestEntity("1")), 1, 10, 0)
        whenever(mockQueryBuilder.page(10, 0)).thenReturn(expected)
        
        val result = baradum.page(10, 0)
        
        assertEquals(expected, result)
        verify(mockQueryBuilder).page(10, 0)
    }

    @Test
    fun `page with only limit uses zero offset`() {
        val expected = Page(emptyList<TestEntity>(), 0, 10, 0)
        whenever(mockQueryBuilder.page(10, 0)).thenReturn(expected)
        
        val result = baradum.page(10)
        
        assertEquals(expected, result)
        verify(mockQueryBuilder).page(10, 0)
    }

    @Test
    fun `page extracts limit from params if provided`() {
        val params = mapOf("limit" to "20", "offset" to "5")
        val expected = Page(emptyList<TestEntity>(), 0, 20, 5)
        whenever(mockQueryBuilder.page(20, 5)).thenReturn(expected)
        
        baradum.withParams(params)
               .page(10, 0)  // Should use 20 and 5 from params instead
        
        verify(mockQueryBuilder).page(20, 5)
    }

    @Test
    fun `page uses default limit when param is invalid`() {
        val params = mapOf("limit" to "invalid")
        val expected = Page(emptyList<TestEntity>(), 0, 10, 0)
        whenever(mockQueryBuilder.page(10, 0)).thenReturn(expected)
        
        baradum.withParams(params)
               .page(10, 0)
        
        verify(mockQueryBuilder).page(10, 0)
    }

    // findFirst Tests
    @Test
    fun `findFirst calls query builder findFirst method`() {
        val entity = TestEntity("1")
        val expected = Optional.of(entity)
        whenever(mockQueryBuilder.findFirst()).thenReturn(expected)
        
        val result = baradum.findFirst()
        
        assertEquals(expected, result)
        verify(mockQueryBuilder).findFirst()
    }

    @Test
    fun `findFirst returns empty optional when no result`() {
        whenever(mockQueryBuilder.findFirst()).thenReturn(Optional.empty())
        
        val result = baradum.findFirst()
        
        assertTrue(result.isEmpty)
    }

    // getWhereConditions Tests
    @Test
    fun `getWhereConditions returns query builder where conditions`() {
        val conditions = "mock-conditions"
        whenever(mockQueryBuilder.getWhereConditions()).thenReturn(conditions)
        
        val result = baradum.getWhereConditions()
        
        assertEquals(conditions, result)
    }

    @Test
    fun `getWhereConditions returns null when no conditions`() {
        whenever(mockQueryBuilder.getWhereConditions()).thenReturn(null)
        
        val result = baradum.getWhereConditions()
        
        assertNull(result)
    }

    // builder Tests
    @Test
    fun `builder allows custom query builder manipulation`() {
        var lambdaCalled = false
        
        val result = baradum.builder { qb ->
            lambdaCalled = true
            assertSame(mockQueryBuilder, qb)
        }
        
        assertTrue(lambdaCalled)
        assertSame(baradum, result)
    }

    @Test
    fun `builder can be chained with other methods`() {
        baradum.allowedFilters(ExactFilter("name"))
               .builder { it.select("id") }
               .allowedSort("createdAt")
        
        verify(mockQueryBuilder).select("id")
    }

    // getBuilder Tests
    @Test
    fun `getBuilder returns query builder instance`() {
        val result = baradum.getBuilder()
        
        assertSame(mockQueryBuilder, result)
    }

    // Integration Tests
    @Test
    fun `chaining multiple configuration methods works`() {
        val params = mapOf("name" to "test", "status" to "active")
        
        val result = baradum
            .withParams(params)
            .allowedFilters(ExactFilter("name"), ExactFilter("status"))
            .allowedSort("name", "createdAt")
            .selects("id", "name")
            .addSelects("email")
        
        assertSame(baradum, result)
        verify(mockQueryBuilder).select("id", "name")
        verify(mockQueryBuilder).addSelect("email")
    }

    @Test
    fun `applying filters and sorts with instance params works`() {
        val params = mapOf("name" to "test")
        whenever(mockQueryBuilder.get()).thenReturn(emptyList())
        
        baradum.withParams(params)
               .allowedFilters(ExactFilter("name"))
               .allowedSort("name")
               .get()
        
        verify(mockQueryBuilder).get()
    }

    // Test entity for mocking
    private data class TestEntity(val id: String)
}
