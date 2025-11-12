package io.github.robertomike.baradum.core.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Test suite for Baradum exception classes
 */
class ExceptionsTest {

    @Test
    fun `BaradumException with message only`() {
        val exception = BaradumException("Test error message")
        
        assertEquals("Test error message", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `BaradumException with message and cause`() {
        val cause = IllegalArgumentException("Root cause")
        val exception = BaradumException("Wrapped error", cause)
        
        assertEquals("Wrapped error", exception.message)
        assertSame(cause, exception.cause)
    }

    @Test
    fun `BaradumException with default constructor`() {
        val exception = BaradumException()
        
        assertEquals("", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `BaradumException is RuntimeException`() {
        val exception = BaradumException("Test")
        
        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `FilterException with message`() {
        val exception = FilterException("Invalid filter configuration")
        
        assertEquals("Invalid filter configuration", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `FilterException is RuntimeException`() {
        val exception = FilterException("Test")
        
        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `FilterException can be thrown and caught`() {
        assertThrows(FilterException::class.java) {
            throw FilterException("Test filter error")
        }
    }

    @Test
    fun `SortableException with message`() {
        val exception = SortableException("Invalid sort configuration")
        
        assertEquals("Invalid sort configuration", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `SortableException is RuntimeException`() {
        val exception = SortableException("Test")
        
        assertTrue(exception is RuntimeException)
    }

    @Test
    fun `SortableException can be thrown and caught`() {
        assertThrows(SortableException::class.java) {
            throw SortableException("Test sortable error")
        }
    }

    @Test
    fun `BaradumException can be used in catch blocks`() {
        try {
            throw BaradumException("Test error")
        } catch (e: BaradumException) {
            assertEquals("Test error", e.message)
        }
    }

    @Test
    fun `FilterException with detailed message`() {
        val field = "email"
        val value = "invalid-email"
        val exception = FilterException("Invalid value '$value' for field '$field'")
        
        assertTrue(exception.message!!.contains("email"))
        assertTrue(exception.message!!.contains("invalid-email"))
    }

    @Test
    fun `SortableException with detailed message`() {
        val field = "unknownField"
        val exception = SortableException("Field '$field' is not allowed for sorting")
        
        assertTrue(exception.message!!.contains("unknownField"))
        assertTrue(exception.message!!.contains("not allowed"))
    }

    @Test
    fun `BaradumException preserves stack trace`() {
        val exception = BaradumException("Test")
        
        assertNotNull(exception.stackTrace)
        assertTrue(exception.stackTrace.isNotEmpty())
    }

    @Test
    fun `nested exception chain`() {
        val root = IllegalStateException("Root problem")
        val wrapped = BaradumException("Wrapped problem", root)
        
        assertEquals(root, wrapped.cause)
        assertEquals("Root problem", wrapped.cause?.message)
    }
}
