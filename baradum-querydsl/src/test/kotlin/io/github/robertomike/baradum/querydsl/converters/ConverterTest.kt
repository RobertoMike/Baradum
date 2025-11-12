package io.github.robertomike.baradum.querydsl.converters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import com.querydsl.core.types.Ops
import com.querydsl.core.types.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for operator and sort converters
 */
class ConverterTest {

    @Test
    fun `OperatorConverter should convert EQUAL correctly`() {
        assertEquals(Ops.EQ, OperatorConverter.toQueryDsl(BaradumOperator.EQUAL))
        assertEquals(BaradumOperator.EQUAL, OperatorConverter.fromQueryDsl(Ops.EQ))
    }

    @Test
    fun `OperatorConverter should convert DIFF correctly`() {
        assertEquals(Ops.NE, OperatorConverter.toQueryDsl(BaradumOperator.DIFF))
        assertEquals(BaradumOperator.DIFF, OperatorConverter.fromQueryDsl(Ops.NE))
    }

    @Test
    fun `OperatorConverter should convert GREATER correctly`() {
        assertEquals(Ops.GT, OperatorConverter.toQueryDsl(BaradumOperator.GREATER))
        assertEquals(BaradumOperator.GREATER, OperatorConverter.fromQueryDsl(Ops.GT))
    }

    @Test
    fun `OperatorConverter should convert GREATER_OR_EQUAL correctly`() {
        assertEquals(Ops.GOE, OperatorConverter.toQueryDsl(BaradumOperator.GREATER_OR_EQUAL))
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, OperatorConverter.fromQueryDsl(Ops.GOE))
    }

    @Test
    fun `OperatorConverter should convert LESS correctly`() {
        assertEquals(Ops.LT, OperatorConverter.toQueryDsl(BaradumOperator.LESS))
        assertEquals(BaradumOperator.LESS, OperatorConverter.fromQueryDsl(Ops.LT))
    }

    @Test
    fun `OperatorConverter should convert LESS_OR_EQUAL correctly`() {
        assertEquals(Ops.LOE, OperatorConverter.toQueryDsl(BaradumOperator.LESS_OR_EQUAL))
        assertEquals(BaradumOperator.LESS_OR_EQUAL, OperatorConverter.fromQueryDsl(Ops.LOE))
    }

    @Test
    fun `OperatorConverter should convert LIKE correctly`() {
        assertEquals(Ops.LIKE, OperatorConverter.toQueryDsl(BaradumOperator.LIKE))
        assertEquals(BaradumOperator.LIKE, OperatorConverter.fromQueryDsl(Ops.LIKE))
    }

    @Test
    fun `OperatorConverter should convert IN correctly`() {
        assertEquals(Ops.IN, OperatorConverter.toQueryDsl(BaradumOperator.IN))
        assertEquals(BaradumOperator.IN, OperatorConverter.fromQueryDsl(Ops.IN))
    }

    @Test
    fun `OperatorConverter should convert NOT_IN correctly`() {
        assertEquals(Ops.NOT_IN, OperatorConverter.toQueryDsl(BaradumOperator.NOT_IN))
        assertEquals(BaradumOperator.NOT_IN, OperatorConverter.fromQueryDsl(Ops.NOT_IN))
    }

    @Test
    fun `OperatorConverter should convert IS_NULL correctly`() {
        assertEquals(Ops.IS_NULL, OperatorConverter.toQueryDsl(BaradumOperator.IS_NULL))
        assertEquals(BaradumOperator.IS_NULL, OperatorConverter.fromQueryDsl(Ops.IS_NULL))
    }

    @Test
    fun `OperatorConverter should convert IS_NOT_NULL correctly`() {
        assertEquals(Ops.IS_NOT_NULL, OperatorConverter.toQueryDsl(BaradumOperator.IS_NOT_NULL))
        assertEquals(BaradumOperator.IS_NOT_NULL, OperatorConverter.fromQueryDsl(Ops.IS_NOT_NULL))
    }

    @Test
    fun `OperatorConverter should convert BETWEEN correctly`() {
        assertEquals(Ops.BETWEEN, OperatorConverter.toQueryDsl(BaradumOperator.BETWEEN))
        assertEquals(BaradumOperator.BETWEEN, OperatorConverter.fromQueryDsl(Ops.BETWEEN))
    }

    @Test
    fun `SortConverter should convert ASC correctly`() {
        assertEquals(Order.ASC, SortConverter.toQueryDsl(SortDirection.ASC))
        assertEquals(SortDirection.ASC, SortConverter.fromQueryDsl(Order.ASC))
    }

    @Test
    fun `SortConverter should convert DESC correctly`() {
        assertEquals(Order.DESC, SortConverter.toQueryDsl(SortDirection.DESC))
        assertEquals(SortDirection.DESC, SortConverter.fromQueryDsl(Order.DESC))
    }

    @Test
    fun `OperatorConverter should handle all BaradumOperator values`() {
        // Ensure all operators can be converted without throwing exceptions
        BaradumOperator.values().forEach { operator ->
            assertNotNull(OperatorConverter.toQueryDsl(operator))
        }
    }

    @Test
    fun `SortConverter should handle all SortDirection values`() {
        // Ensure all directions can be converted without throwing exceptions
        SortDirection.values().forEach { direction ->
            assertNotNull(SortConverter.toQueryDsl(direction))
        }
    }
}
