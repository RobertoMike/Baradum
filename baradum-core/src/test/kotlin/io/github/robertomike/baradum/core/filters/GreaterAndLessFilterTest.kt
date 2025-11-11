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
 * Tests for GreaterFilter and LessFilter string constructors and number parsing
 */
class GreaterAndLessFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
    }

    // GreaterFilter Tests

    @Test
    fun `GreaterFilter with string constructor default orEqual false`() {
        val filter = GreaterFilter("age")
        filter.filterByParam(mockQueryBuilder, "25")
        
        verify(mockQueryBuilder).where(eq("age"), eq(BaradumOperator.GREATER), eq(25), anyOrNull())
    }

    @Test
    fun `GreaterFilter with string constructor and internal name`() {
        val filter = GreaterFilter("userAge", "user_age")
        filter.filterByParam(mockQueryBuilder, "30")
        
        verify(mockQueryBuilder).where(eq("user_age"), eq(BaradumOperator.GREATER), eq(30), anyOrNull())
    }

    @Test
    fun `GreaterFilter with orEqual true uses GREATER_OR_EQUAL`() {
        val filter = GreaterFilter("age", orEqual = true)
        filter.filterByParam(mockQueryBuilder, "18")
        
        verify(mockQueryBuilder).where(eq("age"), eq(BaradumOperator.GREATER_OR_EQUAL), eq(18), anyOrNull())
    }

    @Test
    fun `GreaterFilter parses integer values`() {
        val filter = GreaterFilter("age")
        filter.filterByParam(mockQueryBuilder, "42")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Int)
        assertEquals(42, captor.firstValue)
    }

    @Test
    fun `GreaterFilter parses double values`() {
        val filter = GreaterFilter("price")
        filter.filterByParam(mockQueryBuilder, "19.99")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Double)
        assertEquals(19.99, captor.firstValue)
    }

    @Test
    fun `GreaterFilter parses long values outside Int range`() {
        val filter = GreaterFilter("bigNumber")
        val longValue = (Int.MAX_VALUE.toLong() + 1).toString()
        filter.filterByParam(mockQueryBuilder, longValue)
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Long)
    }

    @Test
    fun `GreaterFilter falls back to string for non-numeric values`() {
        val filter = GreaterFilter("name")
        filter.filterByParam(mockQueryBuilder, "abc123")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is String)
        assertEquals("abc123", captor.firstValue)
    }

    @Test
    fun `GreaterFilter throws exception for empty value`() {
        val filter = GreaterFilter("age")
        
        val exception = assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, "")
        }
        assertTrue(exception.message!!.contains("cannot be empty"))
        assertTrue(exception.message!!.contains("age"))
    }

    @Test
    fun `GreaterFilter throws exception for blank value`() {
        val filter = GreaterFilter("age")
        
        val exception = assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, "   ")
        }
        assertTrue(exception.message!!.contains("cannot be empty"))
    }

    @Test
    fun `GreaterFilter transform returns value unchanged`() {
        val filter = GreaterFilter("age")
        assertEquals("42", filter.transform("42"))
        assertEquals("test", filter.transform("test"))
    }

    // LessFilter Tests

    @Test
    fun `LessFilter with string constructor default orEqual false`() {
        val filter = LessFilter("age")
        filter.filterByParam(mockQueryBuilder, "65")
        
        verify(mockQueryBuilder).where(eq("age"), eq(BaradumOperator.LESS), eq(65), anyOrNull())
    }

    @Test
    fun `LessFilter with string constructor and internal name`() {
        val filter = LessFilter("userAge", "user_age")
        filter.filterByParam(mockQueryBuilder, "30")
        
        verify(mockQueryBuilder).where(eq("user_age"), eq(BaradumOperator.LESS), eq(30), anyOrNull())
    }

    @Test
    fun `LessFilter with orEqual true uses LESS_OR_EQUAL`() {
        val filter = LessFilter("age", orEqual = true)
        filter.filterByParam(mockQueryBuilder, "21")
        
        verify(mockQueryBuilder).where(eq("age"), eq(BaradumOperator.LESS_OR_EQUAL), eq(21), anyOrNull())
    }

    @Test
    fun `LessFilter parses integer values`() {
        val filter = LessFilter("age")
        filter.filterByParam(mockQueryBuilder, "100")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Int)
        assertEquals(100, captor.firstValue)
    }

    @Test
    fun `LessFilter parses double values`() {
        val filter = LessFilter("discount")
        filter.filterByParam(mockQueryBuilder, "0.50")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Double)
        assertEquals(0.50, captor.firstValue)
    }

    @Test
    fun `LessFilter parses long values outside Int range`() {
        val filter = LessFilter("timestamp")
        val longValue = (Int.MAX_VALUE.toLong() + 1000).toString()
        filter.filterByParam(mockQueryBuilder, longValue)
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Long)
    }

    @Test
    fun `LessFilter falls back to string for non-numeric values`() {
        val filter = LessFilter("code")
        filter.filterByParam(mockQueryBuilder, "xyz789")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is String)
        assertEquals("xyz789", captor.firstValue)
    }

    @Test
    fun `LessFilter throws exception for empty value`() {
        val filter = LessFilter("age")
        
        val exception = assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, "")
        }
        assertTrue(exception.message!!.contains("cannot be empty"))
        assertTrue(exception.message!!.contains("age"))
    }

    @Test
    fun `LessFilter throws exception for blank value`() {
        val filter = LessFilter("age")
        
        val exception = assertThrows<FilterException> {
            filter.filterByParam(mockQueryBuilder, "   ")
        }
        assertTrue(exception.message!!.contains("cannot be empty"))
    }

    @Test
    fun `LessFilter transform returns value unchanged`() {
        val filter = LessFilter("age")
        assertEquals("99", filter.transform("99"))
        assertEquals("value", filter.transform("value"))
    }

    // Edge cases

    @Test
    fun `GreaterFilter handles negative numbers`() {
        val filter = GreaterFilter("temperature")
        filter.filterByParam(mockQueryBuilder, "-10")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Int)
        assertEquals(-10, captor.firstValue)
    }

    @Test
    fun `LessFilter handles negative numbers`() {
        val filter = LessFilter("balance")
        filter.filterByParam(mockQueryBuilder, "-5.50")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Double)
        assertEquals(-5.50, captor.firstValue)
    }

    @Test
    fun `GreaterFilter handles zero`() {
        val filter = GreaterFilter("count")
        filter.filterByParam(mockQueryBuilder, "0")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertEquals(0, captor.firstValue)
    }

    @Test
    fun `LessFilter handles zero`() {
        val filter = LessFilter("limit")
        filter.filterByParam(mockQueryBuilder, "0")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertEquals(0, captor.firstValue)
    }
}
