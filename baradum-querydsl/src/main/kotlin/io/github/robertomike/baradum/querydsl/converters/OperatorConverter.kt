package io.github.robertomike.baradum.querydsl.converters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import com.querydsl.core.types.Ops

/**
 * Converter between Baradum operators and QueryDSL operators
 */
object OperatorConverter {
    fun toQueryDsl(operator: BaradumOperator): Ops {
        return when (operator) {
            BaradumOperator.EQUAL -> Ops.EQ
            BaradumOperator.DIFF -> Ops.NE
            BaradumOperator.GREATER -> Ops.GT
            BaradumOperator.GREATER_OR_EQUAL -> Ops.GOE
            BaradumOperator.LESS -> Ops.LT
            BaradumOperator.LESS_OR_EQUAL -> Ops.LOE
            BaradumOperator.LIKE -> Ops.LIKE
            BaradumOperator.NOT_LIKE -> Ops.LIKE // Will be negated
            BaradumOperator.IN -> Ops.IN
            BaradumOperator.NOT_IN -> Ops.NOT_IN
            BaradumOperator.IS_NULL -> Ops.IS_NULL
            BaradumOperator.IS_NOT_NULL -> Ops.IS_NOT_NULL
            BaradumOperator.BETWEEN -> Ops.BETWEEN
        }
    }

    fun fromQueryDsl(operator: Ops): BaradumOperator {
        return when (operator) {
            Ops.EQ -> BaradumOperator.EQUAL
            Ops.NE -> BaradumOperator.DIFF
            Ops.GT -> BaradumOperator.GREATER
            Ops.GOE -> BaradumOperator.GREATER_OR_EQUAL
            Ops.LT -> BaradumOperator.LESS
            Ops.LOE -> BaradumOperator.LESS_OR_EQUAL
            Ops.LIKE -> BaradumOperator.LIKE
            Ops.IN -> BaradumOperator.IN
            Ops.NOT_IN -> BaradumOperator.NOT_IN
            Ops.IS_NULL -> BaradumOperator.IS_NULL
            Ops.IS_NOT_NULL -> BaradumOperator.IS_NOT_NULL
            Ops.BETWEEN -> BaradumOperator.BETWEEN
            else -> BaradumOperator.EQUAL // Default fallback
        }
    }
}
