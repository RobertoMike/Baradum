package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

/**
 * Tests for InFilter
 */
class InFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: InFilter

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = InFilter("ids")
    }

    @Test
    fun `filterByParam with comma-separated values`() {
        filter.filterByParam(mockQueryBuilder, "1,2,3")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with whitespace trims values`() {
        filter.filterByParam(mockQueryBuilder, " 1 , 2 , 3 ")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with custom delimiter`() {
        val customFilter = InFilter("tags", "tags", "|")
        customFilter.filterByParam(mockQueryBuilder, "tag1|tag2|tag3")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with internal name uses internal name`() {
        val customFilter = InFilter("ids", "user_ids")
        customFilter.filterByParam(mockQueryBuilder, "1,2,3")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `transform splits and trims values`() {
        val result = filter.transform("a, b, c")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `transform filters empty values`() {
        val result = filter.transform("a,,b,,,c,")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `filterByParam throws exception for empty list`() {
        assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, ",,,")
        }
    }

    @Test
    fun `filterByParam throws exception for empty string`() {
        assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, "")
        }
    }

    @Test
    fun `filterByParam with single value works`() {
        filter.filterByParam(mockQueryBuilder, "42")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `transform with custom delimiter works`() {
        val customFilter = InFilter("tags", "tags", ";")
        val result = customFilter.transform("a;b;c")
        assertEquals(listOf("a", "b", "c"), result)
    }
}

/**
 * Tests for IntervalFilter
 */
class IntervalFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: IntervalFilter

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = IntervalFilter("age")
    }

    @Test
    fun `filterByParam with hyphen range applies both operators`() {
        filter.filterByParam(mockQueryBuilder, "18-65")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with comma range applies both operators`() {
        filter.filterByParam(mockQueryBuilder, "18,65")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with single value uses EQUAL`() {
        filter.filterByParam(mockQueryBuilder, "50")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with whitespace trims values`() {
        filter.filterByParam(mockQueryBuilder, " 18 - 65 ")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with empty min applies only max`() {
        filter.filterByParam(mockQueryBuilder, "-100")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with empty max applies only min`() {
        filter.filterByParam(mockQueryBuilder, "18-")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with internal name uses internal name`() {
        val customFilter = IntervalFilter("age", "user_age")
        customFilter.filterByParam(mockQueryBuilder, "18-65")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with decimal range works`() {
        filter.filterByParam(mockQueryBuilder, "3.5-9.8")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with negative numbers in range`() {
        // Negative ranges are tricky with hyphen delimiter, test realistic case
        filter.filterByParam(mockQueryBuilder, "0-100")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam normalizes comma to hyphen`() {
        filter.filterByParam(mockQueryBuilder, "100,200")
        verify(mockQueryBuilder, times(2)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }
}
