package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*

/**
 * Tests for ExactFilter string constructors and value conversion
 */
class ExactFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
    }

    @Test
    fun `ExactFilter with string constructor uses EQUAL operator`() {
        val filter = ExactFilter("status")
        filter.filterByParam(mockQueryBuilder, "active")
        
        verify(mockQueryBuilder).where(eq("status"), eq(BaradumOperator.EQUAL), eq("active"), anyOrNull())
    }

    @Test
    fun `ExactFilter with string and internal name`() {
        val filter = ExactFilter("userStatus", "user_status")
        filter.filterByParam(mockQueryBuilder, "pending")
        
        verify(mockQueryBuilder).where(eq("user_status"), eq(BaradumOperator.EQUAL), eq("pending"), anyOrNull())
    }

    @Test
    fun `ExactFilter converts true to Boolean`() {
        val filter = ExactFilter("isActive")
        filter.filterByParam(mockQueryBuilder, "true")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Boolean)
        assertEquals(true, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts false to Boolean`() {
        val filter = ExactFilter("isActive")
        filter.filterByParam(mockQueryBuilder, "false")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Boolean)
        assertEquals(false, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts TRUE case insensitive`() {
        val filter = ExactFilter("enabled")
        filter.filterByParam(mockQueryBuilder, "TRUE")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertEquals(true, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts FALSE case insensitive`() {
        val filter = ExactFilter("disabled")
        filter.filterByParam(mockQueryBuilder, "False")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertEquals(false, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts short integer to Int`() {
        val filter = ExactFilter("age")
        filter.filterByParam(mockQueryBuilder, "42")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Int)
        assertEquals(42, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts long integer to Long`() {
        val filter = ExactFilter("id")
        val longValue = "12345678901"  // > 10 digits
        filter.filterByParam(mockQueryBuilder, longValue)
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Long)
        assertEquals(12345678901L, captor.firstValue)
    }

    @Test
    fun `ExactFilter converts decimal to Double`() {
        val filter = ExactFilter("price")
        filter.filterByParam(mockQueryBuilder, "29.99")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Double)
        assertEquals(29.99, captor.firstValue)
    }

    @Test
    fun `ExactFilter keeps string with letters as String`() {
        val filter = ExactFilter("name")
        filter.filterByParam(mockQueryBuilder, "John123")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is String)
        assertEquals("John123", captor.firstValue)
    }

    @Test
    fun `ExactFilter handles negative integers`() {
        val filter = ExactFilter("temperature")
        filter.filterByParam(mockQueryBuilder, "-15")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Int)
        assertEquals(-15, captor.firstValue)
    }

    @Test
    fun `ExactFilter handles negative decimals`() {
        val filter = ExactFilter("balance")
        filter.filterByParam(mockQueryBuilder, "-10.50")
        
        val captor = argumentCaptor<Any>()
        verify(mockQueryBuilder).where(anyOrNull(), anyOrNull(), captor.capture(), anyOrNull())
        assertTrue(captor.firstValue is Double)
        assertEquals(-10.50, captor.firstValue)
    }

    @Test
    fun `ExactFilter supportBodyOperation returns true`() {
        val filter = ExactFilter("status")
        assertTrue(filter.supportBodyOperation())
    }
}
