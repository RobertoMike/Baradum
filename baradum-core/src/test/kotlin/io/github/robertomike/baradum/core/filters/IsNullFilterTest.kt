package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

/**
 * Tests for IsNullFilter
 */
class IsNullFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: IsNullFilter

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = IsNullFilter("deletedAt")
    }

    @Test
    fun `filterByParam with null string uses IS_NULL`() {
        filter.filterByParam(mockQueryBuilder, "null")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with true uses IS_NULL`() {
        filter.filterByParam(mockQueryBuilder, "true")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with 1 uses IS_NULL`() {
        filter.filterByParam(mockQueryBuilder, "1")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with yes uses IS_NULL`() {
        filter.filterByParam(mockQueryBuilder, "yes")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with not_null uses IS_NOT_NULL`() {
        filter.filterByParam(mockQueryBuilder, "not_null")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with false uses IS_NOT_NULL`() {
        filter.filterByParam(mockQueryBuilder, "false")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with 0 uses IS_NOT_NULL`() {
        filter.filterByParam(mockQueryBuilder, "0")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with no uses IS_NOT_NULL`() {
        filter.filterByParam(mockQueryBuilder, "no")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with internal name uses internal name`() {
        val customFilter = IsNullFilter("deleted", "deleted_at")
        customFilter.filterByParam(mockQueryBuilder, "null")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `transform with uppercase NULL works`() {
        assertTrue(filter.transform("NULL"))
    }

    @Test
    fun `transform with whitespace works`() {
        assertTrue(filter.transform("  null  "))
        assertFalse(filter.transform("  false  "))
    }

    @Test
    fun `transform throws exception for invalid value`() {
        val exception = assertThrows<FilterException> {
            filter.transform("invalid")
        }
        assertTrue(exception.message!!.contains("Invalid value"))
        assertTrue(exception.message!!.contains("deletedAt"))
    }

    @Test
    fun `transform returns true for null`() {
        assertTrue(filter.transform("null"))
    }

    @Test
    fun `transform returns true for true`() {
        assertTrue(filter.transform("true"))
    }

    @Test
    fun `transform returns false for not_null`() {
        assertFalse(filter.transform("not_null"))
    }

    @Test
    fun `transform returns false for false`() {
        assertFalse(filter.transform("false"))
    }
}
