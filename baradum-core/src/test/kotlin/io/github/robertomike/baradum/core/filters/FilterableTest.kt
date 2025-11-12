package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.requests.FilterRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.Optional

/**
 * Comprehensive test suite for Filterable.
 * Tests filter registration, application via request, params map, and FilterRequest collection.
 */
class FilterableTest {

    // Simple test query builder to capture calls
    class TestQueryBuilder : QueryBuilder<TestQueryBuilder> {
        data class WhereCall(val field: String, val operator: BaradumOperator, val value: Any?, val whereOperator: WhereOperator)
        
        val whereCalls = mutableListOf<WhereCall>()
        
        override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): TestQueryBuilder {
            whereCalls.add(WhereCall(field, operator, value, whereOperator))
            return this
        }
        
        override fun where(field: String, value: Any?): TestQueryBuilder {
            whereCalls.add(WhereCall(field, BaradumOperator.EQUAL, value, WhereOperator.AND))
            return this
        }
        
        override fun orderBy(field: String, direction: SortDirection): TestQueryBuilder = this
        override fun select(vararg fields: String): TestQueryBuilder = this
        override fun addSelect(vararg fields: String): TestQueryBuilder = this
        override fun limit(limit: Int): TestQueryBuilder = this
        override fun offset(offset: Long): TestQueryBuilder = this
        override fun get(): List<TestQueryBuilder> = emptyList()
        override fun page(limit: Int, offset: Long): Page<TestQueryBuilder> {
            return Page(emptyList(), 0, 0, 0)
        }
        override fun findFirst(): Optional<TestQueryBuilder> = Optional.empty()
        override fun getWhereConditions(): Any? = null
    }

    // Test request implementation
    class TestRequest(private val params: Map<String, String> = emptyMap()) : BasicRequest<Unit>(Unit) {
        override fun findParamByName(name: String): String? = params[name]
        override val method: String = "GET"
        override val json: String = "{}"
    }

    // Test filter implementation
    open class TestFilter(param: String, internalName: String = param) : Filter<String, TestQueryBuilder>(param, internalName) {
        override fun filterByParam(query: TestQueryBuilder, value: String) {
            query.where(internalName, BaradumOperator.EQUAL, value)
        }
    }

    // Test filter with transform
    class IntFilter(param: String, internalName: String = param) : Filter<Int, TestQueryBuilder>(param, internalName) {
        override fun filterByParam(query: TestQueryBuilder, value: String) {
            query.where(internalName, BaradumOperator.EQUAL, transform(value))
        }
        
        override fun transform(value: String): Int = value.toInt()
    }

    // Test filter that doesn't support body operations
    class NoBodyFilter(param: String) : Filter<String, TestQueryBuilder>(param, param) {
        override fun filterByParam(query: TestQueryBuilder, value: String) {
            query.where(internalName, BaradumOperator.EQUAL, value)
        }
        
        override fun supportBodyOperation(): Boolean = false
    }

    // ============================================
    // Filter Registration Tests
    // ============================================

    @Test
    fun `addFilters with varargs adds filters`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filter1 = TestFilter("name")
        val filter2 = TestFilter("email")
        
        filterable.addFilters(filter1, filter2)
        
        assertEquals(2, filterable.allowedFilters.size)
        assertTrue(filterable.allowedFilters.contains(filter1))
        assertTrue(filterable.allowedFilters.contains(filter2))
    }

    @Test
    fun `addFilters with collection adds filters`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filters = listOf(TestFilter("name"), TestFilter("email"))
        
        filterable.addFilters(filters)
        
        assertEquals(2, filterable.allowedFilters.size)
    }

    @Test
    fun `addFilters can be called multiple times`() {
        val filterable = Filterable<TestQueryBuilder>()
        
        filterable.addFilters(TestFilter("name"))
        filterable.addFilters(TestFilter("email"), TestFilter("age"))
        
        assertEquals(3, filterable.allowedFilters.size)
    }

    @Test
    fun `addFilters with empty varargs is allowed`() {
        val filterable = Filterable<TestQueryBuilder>()
        
        filterable.addFilters()
        
        assertEquals(0, filterable.allowedFilters.size)
    }

    @Test
    fun `addFilters with empty collection is allowed`() {
        val filterable = Filterable<TestQueryBuilder>()
        
        filterable.addFilters(emptyList())
        
        assertEquals(0, filterable.allowedFilters.size)
    }

    // ============================================
    // Apply with BasicRequest Tests
    // ============================================

    @Test
    fun `apply with request filters matching params`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), TestFilter("email"))
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("name" to "John", "email" to "john@example.com"))
        
        filterable.apply(query, request)
        
        assertEquals(2, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
        assertEquals("John", query.whereCalls[0].value)
        assertEquals("email", query.whereCalls[1].field)
        assertEquals("john@example.com", query.whereCalls[1].value)
    }

    @Test
    fun `apply with request skips missing params`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), TestFilter("email"))
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("name" to "John"))
        
        filterable.apply(query, request)
        
        assertEquals(1, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
    }

    @Test
    fun `apply with request uses internal names`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("search", "user_name"))
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("search" to "John"))
        
        filterable.apply(query, request)
        
        assertEquals("user_name", query.whereCalls[0].field)
    }

    @Test
    fun `apply with request respects filter ignore values`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filter = TestFilter("status")
        filter.addIgnore("all")
        filterable.addFilters(filter)
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("status" to "all"))
        
        filterable.apply(query, request)
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `apply with request uses default values`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filter = TestFilter("status")
        filter.setDefaultValue("active")
        filterable.addFilters(filter)
        
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filterable.apply(query, request)
        
        assertEquals(1, query.whereCalls.size)
        assertEquals("active", query.whereCalls[0].value)
    }

    // ============================================
    // Apply with Params Map Tests
    // ============================================

    @Test
    fun `apply with params map filters matching params`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), TestFilter("email"))
        
        val query = TestQueryBuilder()
        val params = mapOf("name" to "John", "email" to "john@example.com")
        
        filterable.apply(query, params)
        
        assertEquals(2, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
        assertEquals("John", query.whereCalls[0].value)
    }

    @Test
    fun `apply with params map skips missing params`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), TestFilter("email"))
        
        val query = TestQueryBuilder()
        val params = mapOf("name" to "John")
        
        filterable.apply(query, params)
        
        assertEquals(1, query.whereCalls.size)
    }

    @Test
    fun `apply with params map skips null values`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), TestFilter("email"))
        
        val query = TestQueryBuilder()
        val params = mapOf<String, String>("name" to "John")
        
        filterable.apply(query, params)
        
        assertEquals(1, query.whereCalls.size)
    }

    @Test
    fun `apply with params map uses internal names`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("search", "user_name"))
        
        val query = TestQueryBuilder()
        val params = mapOf("search" to "John")
        
        filterable.apply(query, params)
        
        assertEquals("user_name", query.whereCalls[0].field)
    }

    // ============================================
    // Apply with FilterRequest Collection Tests
    // ============================================

    @Test
    fun `apply with FilterRequest applies simple filter`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "name",
            value = "John",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(1, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
        assertEquals(BaradumOperator.EQUAL, query.whereCalls[0].operator)
        assertEquals("John", query.whereCalls[0].value)
        assertEquals(WhereOperator.AND, query.whereCalls[0].whereOperator)
    }

    @Test
    fun `apply with FilterRequest throws exception when field not allowed`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "email",
            value = "test@example.com",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        val exception = assertThrows<FilterException> {
            filterable.apply(query, listOf(filterRequest))
        }
        
        assertTrue(exception.message!!.contains("email"))
        assertTrue(exception.message!!.contains("not allowed"))
    }

    @Test
    fun `apply with FilterRequest throws exception when filter doesnt support body`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(NoBodyFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "name",
            value = "John",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        val exception = assertThrows<FilterException> {
            filterable.apply(query, listOf(filterRequest))
        }
        
        assertTrue(exception.message!!.contains("does not support body request"))
    }

    @Test
    fun `apply with FilterRequest uses transform`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(IntFilter("age"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "age",
            value = "25",
            operator = BaradumOperator.GREATER,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(25, query.whereCalls[0].value)
    }

    @Test
    fun `apply with FilterRequest handles IN operator`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(IntFilter("id"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "id",
            value = "1,2,3",
            operator = BaradumOperator.IN,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(1, query.whereCalls.size)
        val result = query.whereCalls[0].value as List<*>
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `apply with FilterRequest handles NOT_IN operator`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("status"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "status",
            value = "draft,archived",
            operator = BaradumOperator.NOT_IN,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        val result = query.whereCalls[0].value as List<*>
        assertEquals(listOf("draft", "archived"), result)
    }

    @Test
    fun `apply with FilterRequest handles IS_NULL operator`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("deletedAt"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "deletedAt",
            value = null,
            operator = BaradumOperator.IS_NULL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(1, query.whereCalls.size)
        assertNull(query.whereCalls[0].value)
        assertEquals(BaradumOperator.IS_NULL, query.whereCalls[0].operator)
    }

    @Test
    fun `apply with FilterRequest handles IS_NOT_NULL operator`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("email"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "email",
            value = null,
            operator = BaradumOperator.IS_NOT_NULL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertNull(query.whereCalls[0].value)
        assertEquals(BaradumOperator.IS_NOT_NULL, query.whereCalls[0].operator)
    }

    @Test
    fun `apply with FilterRequest skips silently when value is null for ignorable operators`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "name",
            value = null,
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        // Should skip silently, not throw exception
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `apply with FilterRequest respects ignore values`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filter = TestFilter("status")
        filter.addIgnore("all")
        filterable.addFilters(filter)
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "status",
            value = "all",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `apply with FilterRequest does not ignore IS_NULL operator`() {
        val filterable = Filterable<TestQueryBuilder>()
        val filter = TestFilter("email")
        filter.addIgnore("all")  // Should not affect IS_NULL
        filterable.addFilters(filter)
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "email",
            value = null,
            operator = BaradumOperator.IS_NULL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(1, query.whereCalls.size)
        assertEquals(BaradumOperator.IS_NULL, query.whereCalls[0].operator)
    }

    @Test
    fun `apply with FilterRequest uses internal name not param name`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("search", "user_name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "search",
            value = "John",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals("user_name", query.whereCalls[0].field)
    }

    @Test
    fun `apply with FilterRequest handles multiple filters`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), IntFilter("age"))
        
        val query = TestQueryBuilder()
        val filterRequests = listOf(
            FilterRequest("name", "John", BaradumOperator.EQUAL, WhereOperator.AND, emptyList()),
            FilterRequest("age", "25", BaradumOperator.GREATER, WhereOperator.AND, emptyList())
        )
        
        filterable.apply(query, filterRequests)
        
        assertEquals(2, query.whereCalls.size)
    }

    @Test
    fun `apply with FilterRequest handles nested subFilters`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"), IntFilter("age"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = null,
            value = null,
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.OR,
            subFilters = listOf(
                FilterRequest("name", "John", BaradumOperator.EQUAL, WhereOperator.AND, emptyList()),
                FilterRequest("age", "25", BaradumOperator.GREATER, WhereOperator.AND, emptyList())
            )
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(2, query.whereCalls.size)
    }

    @Test
    fun `apply with FilterRequest throws exception when field and subFilters both empty`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = null,
            value = null,
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.AND,
            subFilters = emptyList()
        )
        
        val exception = assertThrows<FilterException> {
            filterable.apply(query, listOf(filterRequest))
        }
        
        assertTrue(exception.message!!.contains("cannot be empty"))
    }

    // ============================================
    // Edge Case Tests
    // ============================================

    @Test
    fun `apply with empty filter list does nothing`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        
        filterable.apply(query, emptyList<FilterRequest>())
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `apply with request when no filters registered does nothing`() {
        val filterable = Filterable<TestQueryBuilder>()
        
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("name" to "John"))
        
        filterable.apply(query, request)
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `apply with params map when no filters registered does nothing`() {
        val filterable = Filterable<TestQueryBuilder>()
        
        val query = TestQueryBuilder()
        val params = mapOf("name" to "John")
        
        filterable.apply(query, params)
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `FilterRequest with WhereOperator OR is preserved`() {
        val filterable = Filterable<TestQueryBuilder>()
        filterable.addFilters(TestFilter("name"))
        
        val query = TestQueryBuilder()
        val filterRequest = FilterRequest(
            field = "name",
            value = "John",
            operator = BaradumOperator.EQUAL,
            type = WhereOperator.OR,
            subFilters = emptyList()
        )
        
        filterable.apply(query, listOf(filterRequest))
        
        assertEquals(WhereOperator.OR, query.whereCalls[0].whereOperator)
    }
}
