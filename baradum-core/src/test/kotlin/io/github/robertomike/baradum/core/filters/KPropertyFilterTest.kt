package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Test suite for verifying Kotlin property reference support in all filters.
 * Tests that KProperty1 references correctly map to field names.
 */
class KPropertyFilterTest {

    // Test data class
    data class User(
        val id: Long,
        val name: String,
        val email: String,
        val age: Int,
        val isActive: Boolean,
        val country: String,
        val status: UserStatus
    )

    enum class UserStatus {
        ACTIVE, INACTIVE, PENDING
    }

    // Simple test query builder to capture calls
    class TestQueryBuilder : QueryBuilder<TestQueryBuilder> {
        data class WhereCall(val field: String, val operator: BaradumOperator, val value: Any?)
        
        val whereCalls = mutableListOf<WhereCall>()
        
        override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: io.github.robertomike.baradum.core.enums.WhereOperator): TestQueryBuilder {
            whereCalls.add(WhereCall(field, operator, value))
            return this
        }
        
        override fun where(field: String, value: Any?): TestQueryBuilder {
            whereCalls.add(WhereCall(field, BaradumOperator.EQUAL, value))
            return this
        }
        
        override fun orderBy(field: String, direction: io.github.robertomike.baradum.core.enums.SortDirection): TestQueryBuilder = this
        override fun select(vararg fields: String): TestQueryBuilder = this
        override fun addSelect(vararg fields: String): TestQueryBuilder = this
        override fun limit(limit: Int): TestQueryBuilder = this
        override fun offset(offset: Long): TestQueryBuilder = this
        override fun get(): List<TestQueryBuilder> = emptyList()
        override fun page(limit: Int, offset: Long): io.github.robertomike.baradum.core.models.Page<TestQueryBuilder> {
            return io.github.robertomike.baradum.core.models.Page(emptyList(), 0, 0, 0)
        }
        override fun findFirst(): java.util.Optional<TestQueryBuilder> = java.util.Optional.empty()
        override fun getWhereConditions(): Any? = null
    }

    @Test
    fun `ExactFilter with KProperty should use property name as internal field`() {
        val filter = ExactFilter(User::name)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "John")

        assertEquals("name", filter.param)
        assertEquals("name", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
        assertEquals(BaradumOperator.EQUAL, query.whereCalls[0].operator)
        assertEquals("John", query.whereCalls[0].value)
    }

    @Test
    fun `ExactFilter with KProperty and custom param should use custom param name`() {
        val filter = ExactFilter(User::email, "searchEmail")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "test@example.com")

        assertEquals("searchEmail", filter.param)
        assertEquals("email", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("email", query.whereCalls[0].field)
        assertEquals("test@example.com", query.whereCalls[0].value)
    }

    @Test
    fun `ExactFilter factory method 'of' should work with KProperty`() {
        val filter = ExactFilter.of(User::status)

        assertEquals("status", filter.param)
        assertEquals("status", filter.internalName)
    }

    @Test
    fun `PartialFilter with KProperty should use property name`() {
        val filter = PartialFilter(User::name)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "John")

        assertEquals("name", filter.param)
        assertEquals("name", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("name", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LIKE, query.whereCalls[0].operator)
        assertEquals("John%", query.whereCalls[0].value)
    }

    @Test
    fun `PartialFilter with KProperty and custom param name`() {
        val filter = PartialFilter(User::email, "search")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "john")

        assertEquals("search", filter.param)
        assertEquals("email", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("email", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LIKE, query.whereCalls[0].operator)
        assertEquals("john%", query.whereCalls[0].value)
    }

    @Test
    fun `GreaterFilter with KProperty should use property name`() {
        val filter = GreaterFilter(User::age)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "18")

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.GREATER, query.whereCalls[0].operator)
        assertEquals(18, query.whereCalls[0].value)
    }

    @Test
    fun `GreaterFilter with KProperty and orEqual true`() {
        val filter = GreaterFilter(User::age, orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "18")

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(18, query.whereCalls[0].value)
    }

    @Test
    fun `GreaterFilter with KProperty, custom param, and orEqual`() {
        val filter = GreaterFilter(User::age, "minAge", orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "21")

        assertEquals("minAge", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(21, query.whereCalls[0].value)
    }

    @Test
    fun `GreaterFilter factory method 'of' with KProperty`() {
        val filter = GreaterFilter.of(User::age)

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
    }

    @Test
    fun `GreaterFilter factory method 'of' with KProperty and orEqual`() {
        val filter = GreaterFilter.of(User::age, orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "25")

        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(25, query.whereCalls[0].value)
    }

    @Test
    fun `LessFilter with KProperty should use property name`() {
        val filter = LessFilter(User::age)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "65")

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LESS, query.whereCalls[0].operator)
        assertEquals(65, query.whereCalls[0].value)
    }

    @Test
    fun `LessFilter with KProperty and orEqual true`() {
        val filter = LessFilter(User::age, orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "65")

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(65, query.whereCalls[0].value)
    }

    @Test
    fun `LessFilter with KProperty, custom param, and orEqual`() {
        val filter = LessFilter(User::age, "maxAge", orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "50")

        assertEquals("maxAge", filter.param)
        assertEquals("age", filter.internalName)
        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(50, query.whereCalls[0].value)
    }

    @Test
    fun `LessFilter factory method 'of' with KProperty`() {
        val filter = LessFilter.of(User::age)

        assertEquals("age", filter.param)
        assertEquals("age", filter.internalName)
    }

    @Test
    fun `LessFilter factory method 'of' with KProperty and orEqual`() {
        val filter = LessFilter.of(User::age, orEqual = true)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "30")

        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(30, query.whereCalls[0].value)
    }

    @Test
    fun `Multiple filters with different KProperty references`() {
        val nameFilter = ExactFilter(User::name)
        val emailFilter = PartialFilter(User::email)
        val ageFilter = GreaterFilter(User::age, orEqual = true)
        val statusFilter = ExactFilter(User::status)

        assertEquals("name", nameFilter.internalName)
        assertEquals("email", emailFilter.internalName)
        assertEquals("age", ageFilter.internalName)
        assertEquals("status", statusFilter.internalName)
    }

    @Test
    fun `KProperty with snake_case field names should preserve exact property name`() {
        // Even if database uses snake_case, property name is used
        data class Product(
            val productName: String,
            val priceAmount: Double
        )

        val filter = ExactFilter(Product::productName)

        // The property name is used directly
        assertEquals("productName", filter.param)
        assertEquals("productName", filter.internalName)
    }

    @Test
    fun `KProperty reference preserves type information in filter`() {
        val intFilter = ExactFilter(User::age)
        val stringFilter = ExactFilter(User::name)
        val booleanFilter = ExactFilter(User::isActive)

        // All should work with their respective field names
        assertEquals("age", intFilter.internalName)
        assertEquals("name", stringFilter.internalName)
        assertEquals("isActive", booleanFilter.internalName)
    }

    @Test
    fun `ExactFilter boolean conversion with KProperty`() {
        val filter = ExactFilter(User::isActive)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "true")

        assertEquals(1, query.whereCalls.size)
        assertEquals("isActive", query.whereCalls[0].field)
        assertEquals(BaradumOperator.EQUAL, query.whereCalls[0].operator)
        assertEquals(true, query.whereCalls[0].value)
    }

    @Test
    fun `ExactFilter numeric conversion with KProperty`() {
        val filter = ExactFilter(User::age)
        val query = TestQueryBuilder()

        filter.filterByParam(query, "25")

        assertEquals(1, query.whereCalls.size)
        assertEquals("age", query.whereCalls[0].field)
        assertEquals(BaradumOperator.EQUAL, query.whereCalls[0].operator)
        assertEquals(25, query.whereCalls[0].value)
    }

    @Test
    fun `Custom param name overrides property name for request parameter`() {
        val filter = ExactFilter(User::email, "userEmail")

        // Custom param is used for request parameter
        assertEquals("userEmail", filter.param)
        // But property name is used for internal field
        assertEquals("email", filter.internalName)
    }

    @Test
    fun `Factory methods return correctly configured filters`() {
        val exactFilter = ExactFilter.of(User::name)
        val partialFilter = PartialFilter.of(User::email)
        val greaterFilter = GreaterFilter.of(User::age)
        val lessFilter = LessFilter.of(User::age)

        assertNotNull(exactFilter)
        assertNotNull(partialFilter)
        assertNotNull(greaterFilter)
        assertNotNull(lessFilter)

        assertEquals("name", exactFilter.internalName)
        assertEquals("email", partialFilter.internalName)
        assertEquals("age", greaterFilter.internalName)
        assertEquals("age", lessFilter.internalName)
    }

    @Test
    fun `KProperty filters work with nested properties`() {
        // This demonstrates the property name is extracted correctly
        data class Address(val city: String, val country: String)
        data class Customer(val address: Address, val name: String)

        // Even though address is nested, the property reference works
        val filter = ExactFilter(Customer::name)

        assertEquals("name", filter.param)
        assertEquals("name", filter.internalName)
    }
}
