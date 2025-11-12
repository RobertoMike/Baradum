package io.github.robertomike.baradum.core.sorting

import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.SortableException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.requests.OrderRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.Optional

/**
 * Comprehensive test suite for Sortable and OrderBy
 */
class SortableTest {

    // Simple test query builder to capture calls
    class TestQueryBuilder : QueryBuilder<TestQueryBuilder> {
        data class OrderByCall(val field: String, val direction: SortDirection)
        
        val orderByCalls = mutableListOf<OrderByCall>()
        
        override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): TestQueryBuilder = this
        override fun where(field: String, value: Any?): TestQueryBuilder = this
        
        override fun orderBy(field: String, direction: SortDirection): TestQueryBuilder {
            orderByCalls.add(OrderByCall(field, direction))
            return this
        }
        
        override fun select(vararg fields: String): TestQueryBuilder = this
        override fun addSelect(vararg fields: String): TestQueryBuilder = this
        override fun limit(limit: Int): TestQueryBuilder = this
        override fun offset(offset: Long): TestQueryBuilder = this
        override fun get(): List<TestQueryBuilder> = emptyList()
        override fun page(limit: Int, offset: Long): Page<TestQueryBuilder> = Page(emptyList(), 0, 0, 0)
        override fun findFirst(): Optional<TestQueryBuilder> = Optional.empty()
        override fun getWhereConditions(): Any? = null
    }

    // Test request implementation
    class TestRequest(private val params: Map<String, String> = emptyMap()) : BasicRequest<Unit>(Unit) {
        override fun findParamByName(name: String): String? = params[name]
        override val method: String = "GET"
        override val json: String = "{}"
    }

    // ============================================
    // OrderBy Tests
    // ============================================

    @Test
    fun `OrderBy with same name and internal name`() {
        val orderBy = OrderBy("createdAt")
        
        assertEquals("createdAt", orderBy.name)
        assertEquals("createdAt", orderBy.internalName)
    }

    @Test
    fun `OrderBy with different name and internal name`() {
        val orderBy = OrderBy("created", "created_at")
        
        assertEquals("created", orderBy.name)
        assertEquals("created_at", orderBy.internalName)
    }

    @Test
    fun `OrderBy data class equality`() {
        val order1 = OrderBy("name", "user_name")
        val order2 = OrderBy("name", "user_name")
        
        assertEquals(order1, order2)
        assertEquals(order1.hashCode(), order2.hashCode())
    }

    @Test
    fun `OrderBy data class copy`() {
        val original = OrderBy("name", "user_name")
        val copy = original.copy()
        
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }

    @Test
    fun `OrderBy toString`() {
        val orderBy = OrderBy("name", "user_name")
        val string = orderBy.toString()
        
        assertTrue(string.contains("name"))
        assertTrue(string.contains("user_name"))
    }

    // ============================================
    // Sortable Registration Tests
    // ============================================

    @Test
    fun `addSorts with string varargs`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email", "createdAt")
        
        val query = TestQueryBuilder()
        val sorts = listOf(OrderRequest("name", SortDirection.ASC))
        
        sortable.apply(query, sorts)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
    }

    @Test
    fun `addSorts with OrderBy varargs`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts(
            OrderBy("name", "user_name"),
            OrderBy("email", "user_email")
        )
        
        val query = TestQueryBuilder()
        val sorts = listOf(OrderRequest("name", SortDirection.ASC))
        
        sortable.apply(query, sorts)
        
        assertEquals("user_name", query.orderByCalls[0].field)
    }

    @Test
    fun `addSorts with OrderBy list`() {
        val sortable = Sortable<TestQueryBuilder>()
        val orderList = listOf(
            OrderBy("name"),
            OrderBy("email")
        )
        sortable.addSorts(orderList)
        
        val query = TestQueryBuilder()
        val sorts = listOf(OrderRequest("name", SortDirection.ASC))
        
        sortable.apply(query, sorts)
        
        assertEquals(1, query.orderByCalls.size)
    }

    @Test
    fun `addSorts can be called multiple times`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        sortable.addSorts("email", "age")
        sortable.addSorts(OrderBy("status"))
        
        val query = TestQueryBuilder()
        val sorts = listOf(
            OrderRequest("name", SortDirection.ASC),
            OrderRequest("status", SortDirection.DESC)
        )
        
        sortable.apply(query, sorts)
        
        assertEquals(2, query.orderByCalls.size)
    }

    // ============================================
    // Apply with BasicRequest Tests
    // ============================================

    @Test
    fun `apply with request single ascending sort`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "name"))
        
        sortable.apply(query, request)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[0].direction)
    }

    @Test
    fun `apply with request single descending sort`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "-name"))
        
        sortable.apply(query, request)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[0].direction)
    }

    @Test
    fun `apply with request multiple sorts`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email", "createdAt")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "name,-email,createdAt"))
        
        sortable.apply(query, request)
        
        assertEquals(3, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[0].direction)
        assertEquals("email", query.orderByCalls[1].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[1].direction)
        assertEquals("createdAt", query.orderByCalls[2].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[2].direction)
    }

    @Test
    fun `apply with request when no sort param does nothing`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        sortable.apply(query, request)
        
        assertEquals(0, query.orderByCalls.size)
    }

    @Test
    fun `apply with request trims whitespace from entire string`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "  name,-email  "))
        
        sortable.apply(query, request)
        
        // Note: Trim is applied to the whole string before split, not individual parts
        // So " name " becomes an invalid field (with spaces)
        // The actual implementation trims the whole string, then splits
        assertEquals(2, query.orderByCalls.size)
    }

    @Test
    fun `apply with request uses internal names`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts(OrderBy("created", "created_at"))
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "created"))
        
        sortable.apply(query, request)
        
        assertEquals("created_at", query.orderByCalls[0].field)
    }

    // ============================================
    // Apply with Params Map Tests
    // ============================================

    @Test
    fun `apply with params map single sort`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email")
        
        val query = TestQueryBuilder()
        val params = mapOf("sort" to "name")
        
        sortable.apply(query, params)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[0].direction)
    }

    @Test
    fun `apply with params map descending sort`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val params = mapOf("sort" to "-name")
        
        sortable.apply(query, params)
        
        assertEquals(SortDirection.DESC, query.orderByCalls[0].direction)
    }

    @Test
    fun `apply with params map multiple sorts`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email")
        
        val query = TestQueryBuilder()
        val params = mapOf("sort" to "name,-email")
        
        sortable.apply(query, params)
        
        assertEquals(2, query.orderByCalls.size)
    }

    @Test
    fun `apply with params map when no sort param does nothing`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val params = emptyMap<String, String>()
        
        sortable.apply(query, params)
        
        assertEquals(0, query.orderByCalls.size)
    }

    @Test
    fun `apply with params map trims whitespace`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val params = mapOf("sort" to "  name  ")
        
        sortable.apply(query, params)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
    }

    // ============================================
    // Apply with OrderRequest List Tests
    // ============================================

    @Test
    fun `apply with OrderRequest list`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name", "email")
        
        val query = TestQueryBuilder()
        val sorts = listOf(
            OrderRequest("name", SortDirection.ASC),
            OrderRequest("email", SortDirection.DESC)
        )
        
        sortable.apply(query, sorts)
        
        assertEquals(2, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[0].direction)
        assertEquals("email", query.orderByCalls[1].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[1].direction)
    }

    @Test
    fun `apply with OrderRequest throws exception when field is null`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val sorts = listOf(OrderRequest(null, SortDirection.ASC))
        
        val exception = assertThrows<SortableException> {
            sortable.apply(query, sorts)
        }
        
        assertTrue(exception.message!!.contains("field null"))
    }

    @Test
    fun `apply with OrderRequest throws exception when field not allowed`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val sorts = listOf(OrderRequest("email", SortDirection.ASC))
        
        val exception = assertThrows<SortableException> {
            sortable.apply(query, sorts)
        }
        
        assertTrue(exception.message!!.contains("email"))
        assertTrue(exception.message!!.contains("not valid"))
    }

    @Test
    fun `apply with empty OrderRequest list does nothing`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        
        sortable.apply(query, emptyList())
        
        assertEquals(0, query.orderByCalls.size)
    }

    // ============================================
    // Edge Case Tests
    // ============================================

    @Test
    fun `multiple minus signs in sort param`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "--name"))
        
        sortable.apply(query, request)
        
        assertEquals(1, query.orderByCalls.size)
        assertEquals("name", query.orderByCalls[0].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[0].direction)
    }

    @Test
    fun `sort with only commas creates empty fields`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts("name")
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to ",,,"))
        
        // Should process empty strings which will fail validation
        assertThrows<SortableException> {
            sortable.apply(query, request)
        }
    }

    @Test
    fun `complex sort pattern`() {
        val sortable = Sortable<TestQueryBuilder>()
        sortable.addSorts(
            OrderBy("name", "user_name"),
            OrderBy("created", "created_at"),
            OrderBy("updated", "updated_at")
        )
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("sort" to "-created,name,-updated"))
        
        sortable.apply(query, request)
        
        assertEquals(3, query.orderByCalls.size)
        assertEquals("created_at", query.orderByCalls[0].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[0].direction)
        assertEquals("user_name", query.orderByCalls[1].field)
        assertEquals(SortDirection.ASC, query.orderByCalls[1].direction)
        assertEquals("updated_at", query.orderByCalls[2].field)
        assertEquals(SortDirection.DESC, query.orderByCalls[2].direction)
    }
}
