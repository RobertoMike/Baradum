package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

/**
 * Tests for the generic, backend-agnostic CustomFilter in baradum-core - gives QueryDSL (and any
 * future backend) the same lambda-filter convenience baradum-hefesto's CustomFilter offers.
 */
class CustomFilterTest {

    @Test
    fun `filterByParam invokes the consumer with the query builder and value`() {
        val mockQueryBuilder = mock<QueryBuilder<Any>>()
        var capturedValue: String? = null

        val filter = CustomFilter<QueryBuilder<Any>>("status") { query, value ->
            capturedValue = value
            query.where("status", BaradumOperator.EQUAL, value)
        }

        filter.filterByParam(mockQueryBuilder, "premium")

        assertEquals("premium", capturedValue)
        org.mockito.kotlin.verify(mockQueryBuilder).where("status", BaradumOperator.EQUAL, "premium")
    }

    @Test
    fun `param and internalName both equal the given param name`() {
        val filter = CustomFilter<QueryBuilder<Any>>("myParam") { _, _ -> }

        assertEquals("myParam", filter.param)
        assertEquals("myParam", filter.internalName)
    }
}
