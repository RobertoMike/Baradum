package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

/**
 * Tests for NotInFilter - the mirror image of InFilter, emitting NOT_IN instead of IN.
 */
class NotInFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: NotInFilter

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = NotInFilter("excludedIds", "id")
    }

    @Test
    fun `filterByParam applies NOT_IN with comma-separated values`() {
        filter.filterByParam(mockQueryBuilder, "1,2,3")
        verify(mockQueryBuilder).where("id", BaradumOperator.NOT_IN, listOf("1", "2", "3"))
    }

    @Test
    fun `filterByParam with whitespace trims values`() {
        filter.filterByParam(mockQueryBuilder, " 1 , 2 , 3 ")
        verify(mockQueryBuilder).where("id", BaradumOperator.NOT_IN, listOf("1", "2", "3"))
    }

    @Test
    fun `filterByParam with custom delimiter`() {
        val customFilter = NotInFilter("tags", "tags", "|")
        customFilter.filterByParam(mockQueryBuilder, "a|b|c")
        verify(mockQueryBuilder).where("tags", BaradumOperator.NOT_IN, listOf("a", "b", "c"))
    }

    @Test
    fun `filterByParam throws when the value list is empty`() {
        assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, ",,,")
        }
    }

    @Test
    fun `transform splits, trims, and filters empty values`() {
        assertEquals(listOf("a", "b", "c"), filter.transform("a, ,b,,c"))
    }
}
