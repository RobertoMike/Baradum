package io.github.robertomike.baradum.core.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Comprehensive tests for Page model
 */
class PageTest {

    @Test
    fun `constructor creates page with all properties`() {
        val content = listOf("item1", "item2", "item3")
        val page = Page(content, 100, 10, 20)
        
        assertEquals(content, page.content)
        assertEquals(100, page.totalElements)
        assertEquals(10, page.limit)
        assertEquals(20, page.offset)
    }

    @Test
    fun `totalPages calculates correctly`() {
        val page = Page(emptyList<String>(), 100, 10, 0)
        assertEquals(10, page.totalPages)
    }

    @Test
    fun `totalPages with remainder rounds up`() {
        val page = Page(emptyList<String>(), 95, 10, 0)
        assertEquals(10, page.totalPages)
    }

    @Test
    fun `totalPages with zero limit returns zero`() {
        val page = Page(emptyList<String>(), 100, 0, 0)
        assertEquals(0, page.totalPages)
    }

    @Test
    fun `totalPages with exact division works correctly`() {
        val page = Page(emptyList<String>(), 50, 10, 0)
        assertEquals(5, page.totalPages)
    }

    @Test
    fun `currentPage calculates correctly from offset`() {
        val page = Page(emptyList<String>(), 100, 10, 20)
        assertEquals(2, page.currentPage) // offset 20 / limit 10 = page 2
    }

    @Test
    fun `currentPage at start returns zero`() {
        val page = Page(emptyList<String>(), 100, 10, 0)
        assertEquals(0, page.currentPage)
    }

    @Test
    fun `currentPage with zero limit returns zero`() {
        val page = Page(emptyList<String>(), 100, 0, 20)
        assertEquals(0, page.currentPage)
    }

    @Test
    fun `hasNext returns true when more pages available`() {
        val page = Page(emptyList<String>(), 100, 10, 0)
        assertTrue(page.hasNext)
    }

    @Test
    fun `hasNext returns false on last page`() {
        val page = Page(emptyList<String>(), 100, 10, 90)
        assertFalse(page.hasNext)
    }

    @Test
    fun `hasNext returns false when at end`() {
        val page = Page(emptyList<String>(), 10, 10, 0)
        assertFalse(page.hasNext)
    }

    @Test
    fun `hasPrevious returns true when not on first page`() {
        val page = Page(emptyList<String>(), 100, 10, 20)
        assertTrue(page.hasPrevious)
    }

    @Test
    fun `hasPrevious returns false on first page`() {
        val page = Page(emptyList<String>(), 100, 10, 0)
        assertFalse(page.hasPrevious)
    }

    @Test
    fun `empty page works correctly`() {
        val page = Page(emptyList<String>(), 0, 10, 0)
        
        assertEquals(0, page.totalElements)
        assertEquals(0, page.totalPages)
        assertEquals(0, page.currentPage)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
    }

    @Test
    fun `single item page works correctly`() {
        val page = Page(listOf("item"), 1, 10, 0)
        
        assertEquals(1, page.content.size)
        assertEquals(1, page.totalPages)
        assertEquals(0, page.currentPage)
        assertFalse(page.hasNext)
        assertFalse(page.hasPrevious)
    }

    @Test
    fun `page with large offset works correctly`() {
        val page = Page(emptyList<String>(), 1000, 10, 990)
        
        assertEquals(99, page.currentPage)
        assertFalse(page.hasNext)
        assertTrue(page.hasPrevious)
    }

    @Test
    fun `data class equality works`() {
        val page1 = Page(listOf("a", "b"), 2, 10, 0)
        val page2 = Page(listOf("a", "b"), 2, 10, 0)
        val page3 = Page(listOf("a", "b"), 3, 10, 0)
        
        assertEquals(page1, page2)
        assertNotEquals(page1, page3)
    }

    @Test
    fun `data class copy works`() {
        val original = Page(listOf("a"), 10, 5, 0)
        val copied = original.copy(totalElements = 20)
        
        assertEquals(10, original.totalElements)
        assertEquals(20, copied.totalElements)
        assertEquals(original.content, copied.content)
    }

    @Test
    fun `toString contains all properties`() {
        val page = Page(listOf("item"), 100, 10, 20)
        val str = page.toString()
        
        assertTrue(str.contains("content"))
        assertTrue(str.contains("totalElements"))
        assertTrue(str.contains("limit"))
        assertTrue(str.contains("offset"))
    }
}
