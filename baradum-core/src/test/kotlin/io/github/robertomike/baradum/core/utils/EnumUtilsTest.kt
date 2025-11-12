package io.github.robertomike.baradum.core.utils

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

/**
 * Test suite for EnumUtils extension functions
 */
class EnumUtilsTest {

    enum class TestStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    @Test
    fun `valueOf parses enum from string`() {
        val result = TestStatus::class.java.valueOf("ACTIVE")
        
        assertEquals(TestStatus.ACTIVE, result)
    }

    @Test
    fun `valueOf parses all enum values`() {
        assertEquals(TestStatus.ACTIVE, TestStatus::class.java.valueOf("ACTIVE"))
        assertEquals(TestStatus.INACTIVE, TestStatus::class.java.valueOf("INACTIVE"))
        assertEquals(TestStatus.PENDING, TestStatus::class.java.valueOf("PENDING"))
    }

    @Test
    fun `valueOf throws exception for invalid value`() {
        assertThrows<IllegalArgumentException> {
            TestStatus::class.java.valueOf("INVALID")
        }
    }

    @Test
    fun `valueOf is case sensitive`() {
        assertThrows<IllegalArgumentException> {
            TestStatus::class.java.valueOf("active")
        }
    }

    @Test
    fun `valueOf works with BaradumOperator`() {
        val result = BaradumOperator::class.java.valueOf("EQUAL")
        
        assertEquals(BaradumOperator.EQUAL, result)
    }

    @Test
    fun `valueOf works with SortDirection`() {
        val asc = SortDirection::class.java.valueOf("ASC")
        val desc = SortDirection::class.java.valueOf("DESC")
        
        assertEquals(SortDirection.ASC, asc)
        assertEquals(SortDirection.DESC, desc)
    }

    @Test
    fun `valueOf with all BaradumOperator values`() {
        assertEquals(BaradumOperator.EQUAL, BaradumOperator::class.java.valueOf("EQUAL"))
        assertEquals(BaradumOperator.DIFF, BaradumOperator::class.java.valueOf("DIFF"))
        assertEquals(BaradumOperator.GREATER, BaradumOperator::class.java.valueOf("GREATER"))
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, BaradumOperator::class.java.valueOf("GREATER_OR_EQUAL"))
        assertEquals(BaradumOperator.LESS, BaradumOperator::class.java.valueOf("LESS"))
        assertEquals(BaradumOperator.LESS_OR_EQUAL, BaradumOperator::class.java.valueOf("LESS_OR_EQUAL"))
        assertEquals(BaradumOperator.LIKE, BaradumOperator::class.java.valueOf("LIKE"))
        assertEquals(BaradumOperator.NOT_LIKE, BaradumOperator::class.java.valueOf("NOT_LIKE"))
        assertEquals(BaradumOperator.IN, BaradumOperator::class.java.valueOf("IN"))
        assertEquals(BaradumOperator.NOT_IN, BaradumOperator::class.java.valueOf("NOT_IN"))
        assertEquals(BaradumOperator.IS_NULL, BaradumOperator::class.java.valueOf("IS_NULL"))
        assertEquals(BaradumOperator.IS_NOT_NULL, BaradumOperator::class.java.valueOf("IS_NOT_NULL"))
        assertEquals(BaradumOperator.BETWEEN, BaradumOperator::class.java.valueOf("BETWEEN"))
    }
}
