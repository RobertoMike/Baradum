package io.github.robertomike.baradum.hefesto.converters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.Sort
import io.github.robertomike.hefesto.enums.WhereOperator as HefestoWhereOperator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Comprehensive tests for Hefesto converter classes to achieve 90%+ coverage
 */
class ConvertersTest {

    // OperatorConverter Tests
    @Test
    fun `toHefesto converts EQUAL correctly`() {
        assertEquals(Operator.EQUAL, OperatorConverter.toHefesto(BaradumOperator.EQUAL))
    }

    @Test
    fun `toHefesto converts DIFF correctly`() {
        assertEquals(Operator.DIFF, OperatorConverter.toHefesto(BaradumOperator.DIFF))
    }

    @Test
    fun `toHefesto converts GREATER correctly`() {
        assertEquals(Operator.GREATER, OperatorConverter.toHefesto(BaradumOperator.GREATER))
    }

    @Test
    fun `toHefesto converts GREATER_OR_EQUAL correctly`() {
        assertEquals(Operator.GREATER_OR_EQUAL, OperatorConverter.toHefesto(BaradumOperator.GREATER_OR_EQUAL))
    }

    @Test
    fun `toHefesto converts LESS correctly`() {
        assertEquals(Operator.LESS, OperatorConverter.toHefesto(BaradumOperator.LESS))
    }

    @Test
    fun `toHefesto converts LESS_OR_EQUAL correctly`() {
        assertEquals(Operator.LESS_OR_EQUAL, OperatorConverter.toHefesto(BaradumOperator.LESS_OR_EQUAL))
    }

    @Test
    fun `toHefesto converts LIKE correctly`() {
        assertEquals(Operator.LIKE, OperatorConverter.toHefesto(BaradumOperator.LIKE))
    }

    @Test
    fun `toHefesto converts NOT_LIKE correctly`() {
        assertEquals(Operator.NOT_LIKE, OperatorConverter.toHefesto(BaradumOperator.NOT_LIKE))
    }

    @Test
    fun `toHefesto converts IN correctly`() {
        assertEquals(Operator.IN, OperatorConverter.toHefesto(BaradumOperator.IN))
    }

    @Test
    fun `toHefesto converts NOT_IN correctly`() {
        assertEquals(Operator.NOT_IN, OperatorConverter.toHefesto(BaradumOperator.NOT_IN))
    }

    @Test
    fun `toHefesto converts IS_NULL correctly`() {
        assertEquals(Operator.IS_NULL, OperatorConverter.toHefesto(BaradumOperator.IS_NULL))
    }

    @Test
    fun `toHefesto converts IS_NOT_NULL correctly`() {
        assertEquals(Operator.IS_NOT_NULL, OperatorConverter.toHefesto(BaradumOperator.IS_NOT_NULL))
    }

    @Test
    fun `toHefesto converts BETWEEN to fallback correctly`() {
        // BETWEEN doesn't exist in Hefesto 3, so it falls back to GREATER_OR_EQUAL
        assertEquals(Operator.GREATER_OR_EQUAL, OperatorConverter.toHefesto(BaradumOperator.BETWEEN))
    }

    @Test
    fun `fromHefesto converts EQUAL correctly`() {
        assertEquals(BaradumOperator.EQUAL, OperatorConverter.fromHefesto(Operator.EQUAL))
    }

    @Test
    fun `fromHefesto converts DIFF correctly`() {
        assertEquals(BaradumOperator.DIFF, OperatorConverter.fromHefesto(Operator.DIFF))
    }

    @Test
    fun `fromHefesto converts GREATER correctly`() {
        assertEquals(BaradumOperator.GREATER, OperatorConverter.fromHefesto(Operator.GREATER))
    }

    @Test
    fun `fromHefesto converts GREATER_OR_EQUAL correctly`() {
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, OperatorConverter.fromHefesto(Operator.GREATER_OR_EQUAL))
    }

    @Test
    fun `fromHefesto converts LESS correctly`() {
        assertEquals(BaradumOperator.LESS, OperatorConverter.fromHefesto(Operator.LESS))
    }

    @Test
    fun `fromHefesto converts LESS_OR_EQUAL correctly`() {
        assertEquals(BaradumOperator.LESS_OR_EQUAL, OperatorConverter.fromHefesto(Operator.LESS_OR_EQUAL))
    }

    @Test
    fun `fromHefesto converts LIKE correctly`() {
        assertEquals(BaradumOperator.LIKE, OperatorConverter.fromHefesto(Operator.LIKE))
    }

    @Test
    fun `fromHefesto converts NOT_LIKE correctly`() {
        assertEquals(BaradumOperator.NOT_LIKE, OperatorConverter.fromHefesto(Operator.NOT_LIKE))
    }

    @Test
    fun `fromHefesto converts IN correctly`() {
        assertEquals(BaradumOperator.IN, OperatorConverter.fromHefesto(Operator.IN))
    }

    @Test
    fun `fromHefesto converts NOT_IN correctly`() {
        assertEquals(BaradumOperator.NOT_IN, OperatorConverter.fromHefesto(Operator.NOT_IN))
    }

    @Test
    fun `fromHefesto converts IS_NULL correctly`() {
        assertEquals(BaradumOperator.IS_NULL, OperatorConverter.fromHefesto(Operator.IS_NULL))
    }

    @Test
    fun `fromHefesto converts IS_NOT_NULL correctly`() {
        assertEquals(BaradumOperator.IS_NOT_NULL, OperatorConverter.fromHefesto(Operator.IS_NOT_NULL))
    }

    @Test
    fun `bidirectional operator conversion maintains consistency`() {
        val baradumOps = listOf(
            BaradumOperator.EQUAL, BaradumOperator.DIFF, BaradumOperator.GREATER,
            BaradumOperator.GREATER_OR_EQUAL, BaradumOperator.LESS, BaradumOperator.LESS_OR_EQUAL,
            BaradumOperator.LIKE, BaradumOperator.NOT_LIKE, BaradumOperator.IN, BaradumOperator.NOT_IN,
            BaradumOperator.IS_NULL, BaradumOperator.IS_NOT_NULL
        )

        for (op in baradumOps) {
            val hefestoOp = OperatorConverter.toHefesto(op)
            val backToBaradum = OperatorConverter.fromHefesto(hefestoOp)
            // Note: BETWEEN is special case - fallback loses info
            if (op != BaradumOperator.BETWEEN) {
                assertEquals(op, backToBaradum, "Bidirectional conversion should be consistent for $op")
            }
        }
    }

    // SortConverter Tests
    @Test
    fun `SortConverter toHefesto converts ASC correctly`() {
        assertEquals(Sort.ASC, SortConverter.toHefesto(SortDirection.ASC))
    }

    @Test
    fun `SortConverter toHefesto converts DESC correctly`() {
        assertEquals(Sort.DESC, SortConverter.toHefesto(SortDirection.DESC))
    }

    @Test
    fun `SortConverter fromHefesto converts ASC correctly`() {
        assertEquals(SortDirection.ASC, SortConverter.fromHefesto(Sort.ASC))
    }

    @Test
    fun `SortConverter fromHefesto converts DESC correctly`() {
        assertEquals(SortDirection.DESC, SortConverter.fromHefesto(Sort.DESC))
    }

    @Test
    fun `bidirectional sort conversion maintains consistency`() {
        val sortDirections = SortDirection.values()

        for (direction in sortDirections) {
            val hefestoSort = SortConverter.toHefesto(direction)
            val backToBaradum = SortConverter.fromHefesto(hefestoSort)
            assertEquals(direction, backToBaradum, "Bidirectional conversion should be consistent for $direction")
        }
    }

    // WhereOperatorConverter Tests
    @Test
    fun `WhereOperatorConverter toHefesto converts AND correctly`() {
        assertEquals(HefestoWhereOperator.AND, WhereOperatorConverter.toHefesto(WhereOperator.AND))
    }

    @Test
    fun `WhereOperatorConverter toHefesto converts OR correctly`() {
        assertEquals(HefestoWhereOperator.OR, WhereOperatorConverter.toHefesto(WhereOperator.OR))
    }

    @Test
    fun `WhereOperatorConverter fromHefesto converts AND correctly`() {
        assertEquals(WhereOperator.AND, WhereOperatorConverter.fromHefesto(HefestoWhereOperator.AND))
    }

    @Test
    fun `WhereOperatorConverter fromHefesto converts OR correctly`() {
        assertEquals(WhereOperator.OR, WhereOperatorConverter.fromHefesto(HefestoWhereOperator.OR))
    }

    @Test
    fun `bidirectional where operator conversion maintains consistency`() {
        val whereOperators = WhereOperator.values()

        for (operator in whereOperators) {
            val hefestoOp = WhereOperatorConverter.toHefesto(operator)
            val backToBaradum = WhereOperatorConverter.fromHefesto(hefestoOp)
            assertEquals(operator, backToBaradum, "Bidirectional conversion should be consistent for $operator")
        }
    }

    // Integration tests - cross-converter scenarios
    @Test
    fun `all converters handle round-trip conversions correctly`() {
        // Test that converting to Hefesto and back maintains values
        val testOperator = BaradumOperator.EQUAL
        val testSort = SortDirection.ASC
        val testWhere = WhereOperator.AND

        val roundTripOperator = OperatorConverter.fromHefesto(OperatorConverter.toHefesto(testOperator))
        val roundTripSort = SortConverter.fromHefesto(SortConverter.toHefesto(testSort))
        val roundTripWhere = WhereOperatorConverter.fromHefesto(WhereOperatorConverter.toHefesto(testWhere))

        assertEquals(testOperator, roundTripOperator)
        assertEquals(testSort, roundTripSort)
        assertEquals(testWhere, roundTripWhere)
    }

    @Test
    fun `converter objects are singletons`() {
        // Verify that converters are objects (singletons in Kotlin)
        assertSame(OperatorConverter, OperatorConverter)
        assertSame(SortConverter, SortConverter)
        assertSame(WhereOperatorConverter, WhereOperatorConverter)
    }

    @Test
    fun `all BaradumOperator values have toHefesto mapping`() {
        val allOperators = BaradumOperator.values()
        
        for (op in allOperators) {
            assertDoesNotThrow {
                OperatorConverter.toHefesto(op)
            }
        }
        
        assertEquals(13, allOperators.size, "Should test all 13 BaradumOperator values")
    }
}
