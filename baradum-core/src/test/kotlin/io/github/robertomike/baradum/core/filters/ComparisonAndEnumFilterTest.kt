package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

/**
 * Tests for ComparisonFilter
 */
class ComparisonFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: ComparisonFilter

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = ComparisonFilter("age")
    }

    @Test
    fun `parseOperatorAndValue with greater than`() {
        filter.filterByParam(mockQueryBuilder, ">25")
        verify(mockQueryBuilder).where("age", BaradumOperator.GREATER, "25")
    }

    @Test
    fun `parseOperatorAndValue with greater or equal`() {
        filter.filterByParam(mockQueryBuilder, ">=18")
        verify(mockQueryBuilder).where("age", BaradumOperator.GREATER_OR_EQUAL, "18")
    }

    @Test
    fun `parseOperatorAndValue with less than`() {
        filter.filterByParam(mockQueryBuilder, "<100")
        verify(mockQueryBuilder).where("age", BaradumOperator.LESS, "100")
    }

    @Test
    fun `parseOperatorAndValue with less or equal`() {
        filter.filterByParam(mockQueryBuilder, "<=65")
        verify(mockQueryBuilder).where("age", BaradumOperator.LESS_OR_EQUAL, "65")
    }

    @Test
    fun `parseOperatorAndValue with not equal`() {
        filter.filterByParam(mockQueryBuilder, "!=0")
        verify(mockQueryBuilder).where("age", BaradumOperator.DIFF, "0")
    }

    @Test
    fun `parseOperatorAndValue with no operator defaults to equal`() {
        filter.filterByParam(mockQueryBuilder, "25")
        verify(mockQueryBuilder).where("age", BaradumOperator.EQUAL, "25")
    }

    @Test
    fun `filterByParam trims whitespace after operator`() {
        filter.filterByParam(mockQueryBuilder, "> 25  ")
        verify(mockQueryBuilder).where("age", BaradumOperator.GREATER, "25")
    }

    @Test
    fun `filterByParam with internal name uses internal name`() {
        val filterWithInternal = ComparisonFilter("age", "user_age")
        filterWithInternal.filterByParam(mockQueryBuilder, ">18")
        verify(mockQueryBuilder).where("user_age", BaradumOperator.GREATER, "18")
    }

    @Test
    fun `filterByParam throws exception for empty value after operator`() {
        assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, ">")
        }
    }

    @Test
    fun `filterByParam throws exception for whitespace only value`() {
        assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, ">   ")
        }
    }

    @Test
    fun `transform returns value unchanged`() {
        assertEquals("test", filter.transform("test"))
    }

    @Test
    fun `parseOperatorAndValue with negative numbers works`() {
        filter.filterByParam(mockQueryBuilder, ">-10")
        verify(mockQueryBuilder).where("age", BaradumOperator.GREATER, "-10")
    }

    @Test
    fun `parseOperatorAndValue with decimal numbers works`() {
        filter.filterByParam(mockQueryBuilder, ">=3.14")
        verify(mockQueryBuilder).where("age", BaradumOperator.GREATER_OR_EQUAL, "3.14")
    }
}

/**
 * Tests for EnumFilter
 */
class EnumFilterTest {

    private enum class TestStatus {
        ACTIVE, INACTIVE, PENDING, ARCHIVED
    }

    private lateinit var mockQueryBuilder: QueryBuilder<Any>
    private lateinit var filter: EnumFilter<TestStatus, QueryBuilder<Any>>

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
        filter = EnumFilter("status", "status", TestStatus::class.java)
    }

    @Test
    fun `filterByParam with single value uses EQUAL operator`() {
        filter.filterByParam(mockQueryBuilder, "ACTIVE")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `filterByParam with multiple values uses IN operator`() {
        filter.filterByParam(mockQueryBuilder, "ACTIVE,PENDING,ARCHIVED")
        
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull<Any>(), anyOrNull())
    }

    @Test
    fun `filterByParam trims whitespace in multiple values`() {
        filter.filterByParam(mockQueryBuilder, " ACTIVE , PENDING , INACTIVE ")
        
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull<Any>(), anyOrNull())
    }

    @Test
    fun `filterByParam with internal name uses internal name`() {
        val filterWithInternal = EnumFilter<TestStatus, QueryBuilder<Any>>("status", "user_status", TestStatus::class.java)
        filterWithInternal.filterByParam(mockQueryBuilder, "ACTIVE")
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `transform converts string to enum`() {
        assertEquals(TestStatus.ACTIVE, filter.transform("ACTIVE"))
        assertEquals(TestStatus.PENDING, filter.transform("PENDING"))
    }

    @Test
    fun `transform trims whitespace`() {
        assertEquals(TestStatus.ACTIVE, filter.transform("  ACTIVE  "))
    }

    @Test
    fun `transform throws FilterException for invalid enum value`() {
        val exception = assertThrows<FilterException> {
            filter.transform("INVALID")
        }
        
        assertTrue(exception.message!!.contains("Invalid value 'INVALID'"))
        assertTrue(exception.message!!.contains("ACTIVE"))
        assertTrue(exception.message!!.contains("PENDING"))
    }

    @Test
    fun `filterByParam ignores empty values in comma-separated list`() {
        filter.filterByParam(mockQueryBuilder, "ACTIVE,,PENDING,,,INACTIVE,")
        
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull<Any>(), anyOrNull())
    }

    @Test
    fun `filterByParam with all empty values does not call query builder`() {
        filter.filterByParam(mockQueryBuilder, ",,,")
        
        verify(mockQueryBuilder, never()).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `supportBodyOperation returns true`() {
        assertTrue(filter.supportBodyOperation())
    }

    @Test
    fun `filterByParam with two values works`() {
        filter.filterByParam(mockQueryBuilder, "ACTIVE,PENDING")
        
        verify(mockQueryBuilder, times(1)).where(anyOrNull(), anyOrNull(), anyOrNull<Any>(), anyOrNull())
    }
}
