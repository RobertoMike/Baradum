package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.core.requests.BasicRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.Optional

/**
 * Comprehensive test suite for the Filter base class.
 * Tests default values, ignore values, operators, KProperty support, and all base functionality.
 */
class FilterTest {

    // Test data class for KProperty tests
    data class User(
        val id: Long,
        val name: String,
        val email: String,
        val createdAt: String
    )

    // Simple test query builder to capture calls
    class TestQueryBuilder : QueryBuilder<TestQueryBuilder> {
        data class WhereCall(val field: String, val operator: BaradumOperator, val value: Any?)
        
        val whereCalls = mutableListOf<WhereCall>()
        
        override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): TestQueryBuilder {
            whereCalls.add(WhereCall(field, operator, value))
            return this
        }
        
        override fun where(field: String, value: Any?): TestQueryBuilder {
            whereCalls.add(WhereCall(field, BaradumOperator.EQUAL, value))
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

    // Concrete implementation for testing
    open class TestFilter(param: String, internalName: String = param) : Filter<String, TestQueryBuilder>(param, internalName) {
        var lastFilteredValue: String? = null
        
        override fun filterByParam(query: TestQueryBuilder, value: String) {
            lastFilteredValue = value
            query.where(internalName, BaradumOperator.EQUAL, value)
        }
        
        // Public methods to test protected methods
        fun testGetOperator(value: String) = getOperator(value)
        fun testCleanValue(value: String) = cleanValue(value)
    }

    // Concrete implementation for KProperty testing
    class TestPropertyFilter : Filter<String, TestQueryBuilder> {
        var lastFilteredValue: String? = null
        
        constructor(property: kotlin.reflect.KProperty1<*, *>, param: String? = null) : super(property, param)
        
        override fun filterByParam(query: TestQueryBuilder, value: String) {
            lastFilteredValue = value
            query.where(internalName, BaradumOperator.EQUAL, value)
        }
    }

    // ============================================
    // Constructor Tests
    // ============================================

    @Test
    fun `constructor with same param and internal name`() {
        val filter = TestFilter("name")
        
        assertEquals("name", filter.param)
        assertEquals("name", filter.internalName)
    }

    @Test
    fun `constructor with different param and internal name`() {
        val filter = TestFilter("searchName", "user_name")
        
        assertEquals("searchName", filter.param)
        assertEquals("user_name", filter.internalName)
    }

    @Test
    fun `KProperty constructor uses property name as both param and internal name`() {
        val filter = TestPropertyFilter(User::name)
        
        assertEquals("name", filter.param)
        assertEquals("name", filter.internalName)
    }

    @Test
    fun `KProperty constructor with custom param`() {
        val filter = TestPropertyFilter(User::email, "searchEmail")
        
        assertEquals("searchEmail", filter.param)
        assertEquals("email", filter.internalName)
    }

    @Test
    fun `KProperty constructor with nested property path`() {
        val filter = TestPropertyFilter(User::createdAt, "dateFilter")
        
        assertEquals("dateFilter", filter.param)
        assertEquals("createdAt", filter.internalName)
    }

    // ============================================
    // Default Value Tests
    // ============================================

    @Test
    fun `setDefaultValue sets default value`() {
        val filter = TestFilter("status")
        filter.setDefaultValue("active")
        
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filter.filterByParam(query, request)
        
        assertEquals(1, query.whereCalls.size)
        assertEquals("active", query.whereCalls[0].value)
    }

    @Test
    fun `default value is used when param not in request`() {
        val filter = TestFilter("status")
        filter.setDefaultValue("pending")
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filter.filterByParam(query, request)
        
        assertEquals("pending", filter.lastFilteredValue)
    }

    @Test
    fun `request value overrides default value`() {
        val filter = TestFilter("status")
        filter.setDefaultValue("pending")
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("status" to "active"))
        
        filter.filterByParam(query, request)
        
        assertEquals("active", filter.lastFilteredValue)
    }

    @Test
    fun `setDefaultValue returns filter for chaining`() {
        val filter = TestFilter("status")
        val result = filter.setDefaultValue("active")
        
        assertSame(filter, result)
    }

    @Test
    fun `null default value is allowed`() {
        val filter = TestFilter("status")
        filter.setDefaultValue(null)
        
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filter.filterByParam(query, request)
        
        assertEquals(0, query.whereCalls.size)
    }

    // ============================================
    // Ignore Values Tests
    // ============================================

    @Test
    fun `addIgnore adds single value`() {
        val filter = TestFilter("status")
        filter.addIgnore("all")
        
        assertTrue(filter.ignore("all"))
    }

    @Test
    fun `addIgnore adds multiple values`() {
        val filter = TestFilter("status")
        filter.addIgnore("all", "none", "any")
        
        assertTrue(filter.ignore("all"))
        assertTrue(filter.ignore("none"))
        assertTrue(filter.ignore("any"))
    }

    @Test
    fun `ignore returns false for non-ignored values`() {
        val filter = TestFilter("status")
        filter.addIgnore("all")
        
        assertFalse(filter.ignore("active"))
    }

    @Test
    fun `ignore trims whitespace from value`() {
        val filter = TestFilter("status")
        filter.addIgnore("all")
        
        assertTrue(filter.ignore("  all  "))
    }

    @Test
    fun `addIgnore returns filter for chaining`() {
        val filter = TestFilter("status")
        val result = filter.addIgnore("all", "none")
        
        assertSame(filter, result)
    }

    @Test
    fun `addIgnore can be called multiple times`() {
        val filter = TestFilter("status")
        filter.addIgnore("all")
        filter.addIgnore("none")
        
        assertTrue(filter.ignore("all"))
        assertTrue(filter.ignore("none"))
    }

    @Test
    fun `ignored value skips filtering with request`() {
        val filter = TestFilter("status")
        filter.addIgnore("all")
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("status" to "all"))
        
        filter.filterByParam(query, request)
        
        assertEquals(0, query.whereCalls.size)
        assertNull(filter.lastFilteredValue)
    }

    @Test
    fun `ignored value skips filtering with body map`() {
        val filter = TestFilter("status").addIgnore("all")
        val query = TestQueryBuilder()
        val body = mapOf("status" to "all")
        
        filter.filterByParam(query, body)
        
        assertEquals(0, query.whereCalls.size)
    }

    // ============================================
    // Operator Detection Tests (getOperator)
    // ============================================

    @Test
    fun `getOperator detects greater than`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.GREATER, filter.testGetOperator(">100"))
    }

    @Test
    fun `getOperator detects greater or equal`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, filter.testGetOperator(">=100"))
    }

    @Test
    fun `getOperator detects less than`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.LESS, filter.testGetOperator("<100"))
    }

    @Test
    fun `getOperator detects less or equal`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.LESS_OR_EQUAL, filter.testGetOperator("<=100"))
    }

    @Test
    fun `getOperator detects diff`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.DIFF, filter.testGetOperator("<>100"))
    }

    @Test
    fun `getOperator defaults to EQUAL`() {
        val filter = TestFilter("value")
        assertEquals(BaradumOperator.EQUAL, filter.testGetOperator("100"))
    }

    @Test
    fun `getOperator prioritizes two-character operators`() {
        val filter = TestFilter("value")
        // >= should be detected before >
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, filter.testGetOperator(">=100"))
        // <= should be detected before <
        assertEquals(BaradumOperator.LESS_OR_EQUAL, filter.testGetOperator("<=100"))
    }

    // ============================================
    // Value Cleaning Tests (cleanValue)
    // ============================================

    @Test
    fun `cleanValue removes greater than operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue(">100"))
    }

    @Test
    fun `cleanValue removes greater or equal operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue(">=100"))
    }

    @Test
    fun `cleanValue removes less than operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue("<100"))
    }

    @Test
    fun `cleanValue removes less or equal operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue("<=100"))
    }

    @Test
    fun `cleanValue removes diff operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue("<>100"))
    }

    @Test
    fun `cleanValue returns value unchanged when no operator`() {
        val filter = TestFilter("value")
        assertEquals("100", filter.testCleanValue("100"))
    }

    // ============================================
    // Request Filtering Tests
    // ============================================

    @Test
    fun `filterByParam with request does nothing when param not exists and no default`() {
        val filter = TestFilter("status")
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filter.filterByParam(query, request)
        
        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `filterByParam with request uses param value`() {
        val filter = TestFilter("status")
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("status" to "active"))
        
        filter.filterByParam(query, request)
        
        assertEquals("active", filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with request ignores ignored value`() {
        val filter = TestFilter("status")
        filter.addIgnore("skip")
        val query = TestQueryBuilder()
        val request = TestRequest(mapOf("status" to "skip"))
        
        filter.filterByParam(query, request)
        
        assertNull(filter.lastFilteredValue)
    }

    // ============================================
    // Body Map Filtering Tests
    // ============================================

    @Test
    fun `filterByParam with body map uses param value`() {
        val filter = TestFilter("status")
        val query = TestQueryBuilder()
        val body = mapOf("status" to "active")
        
        filter.filterByParam(query, body)
        
        assertEquals("active", filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with body map uses default when param missing`() {
        val filter = TestFilter("status")
        filter.setDefaultValue("pending")
        val query = TestQueryBuilder()
        val body = mapOf<String, Any?>()
        
        filter.filterByParam(query, body)
        
        assertEquals("pending", filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with body map does nothing when no param and no default`() {
        val filter = TestFilter("status")
        val query = TestQueryBuilder()
        val body = mapOf<String, Any?>()
        
        filter.filterByParam(query, body)
        
        assertNull(filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with body map converts non-string values to string`() {
        val filter = TestFilter("age")
        val query = TestQueryBuilder()
        val body = mapOf("age" to 25)
        
        filter.filterByParam(query, body)
        
        assertEquals("25", filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with body map handles null values`() {
        val filter = TestFilter("status")
        val query = TestQueryBuilder()
        val body = mapOf<String, Any?>("status" to null)
        
        filter.filterByParam(query, body)
        
        assertNull(filter.lastFilteredValue)
    }

    @Test
    fun `filterByParam with body map respects ignored values`() {
        val filter = TestFilter("status")
        filter.addIgnore("skip")
        val query = TestQueryBuilder()
        val body = mapOf("status" to "skip")
        
        filter.filterByParam(query, body)
        
        assertNull(filter.lastFilteredValue)
    }

    // ============================================
    // Transform Tests
    // ============================================

    @Test
    fun `transform returns value as-is by default`() {
        val filter = TestFilter("value")
        
        val result = filter.transform("test")
        
        assertEquals("test", result)
    }

    @Test
    fun `transform can be overridden`() {
        val filter = object : Filter<Int, TestQueryBuilder>("value", "value") {
            override fun filterByParam(query: TestQueryBuilder, value: String) {}
            override fun transform(value: String): Int = value.toInt()
        }
        
        val result = filter.transform("123")
        
        assertEquals(123, result)
    }

    // ============================================
    // Body Operation Support Tests
    // ============================================

    @Test
    fun `supportBodyOperation returns true by default`() {
        val filter = TestFilter("status")
        
        assertTrue(filter.supportBodyOperation())
    }

    @Test
    fun `supportBodyOperation can be overridden`() {
        val filter = object : TestFilter("status") {
            override fun supportBodyOperation(): Boolean = false
        }
        
        assertFalse(filter.supportBodyOperation())
    }

    // ============================================
    // Chaining Tests
    // ============================================

    @Test
    fun `can chain setDefaultValue and addIgnore`() {
        val filter = TestFilter("status")
        filter.setDefaultValue("pending")
        filter.addIgnore("all", "none")
        
        val query = TestQueryBuilder()
        val request = TestRequest(emptyMap())
        
        filter.filterByParam(query, request)
        
        assertEquals("pending", filter.lastFilteredValue)
        assertTrue(filter.ignore("all"))
    }

    @Test
    fun `can chain multiple addIgnore calls`() {
        val filter = TestFilter("status")
            .addIgnore("all")
            .addIgnore("none")
            .addIgnore("any")
        
        assertTrue(filter.ignore("all"))
        assertTrue(filter.ignore("none"))
        assertTrue(filter.ignore("any"))
    }

    // ============================================
    // Edge Case Tests
    // ============================================

    @Test
    fun `filterByParam with string value directly`() {
        val filter = TestFilter("status", "user_status")
        val query = TestQueryBuilder()
        
        filter.filterByParam(query, "active")
        
        assertEquals("active", filter.lastFilteredValue)
        assertEquals(1, query.whereCalls.size)
        assertEquals("user_status", query.whereCalls[0].field)
    }
}
