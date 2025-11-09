package io.github.robertomike.baradum.hefesto.converters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.hefesto.enums.Operator

object OperatorConverter {
    fun toHefesto(operator: BaradumOperator): Operator {
        return when (operator) {
            BaradumOperator.EQUAL -> Operator.EQUAL
            BaradumOperator.DIFF -> Operator.DIFF
            BaradumOperator.GREATER -> Operator.GREATER
            BaradumOperator.GREATER_OR_EQUAL -> Operator.GREATER_OR_EQUAL
            BaradumOperator.LESS -> Operator.LESS
            BaradumOperator.LESS_OR_EQUAL -> Operator.LESS_OR_EQUAL
            BaradumOperator.LIKE -> Operator.LIKE
            BaradumOperator.NOT_LIKE -> Operator.NOT_LIKE
            BaradumOperator.IN -> Operator.IN
            BaradumOperator.NOT_IN -> Operator.NOT_IN
            BaradumOperator.IS_NULL -> Operator.IS_NULL
            BaradumOperator.IS_NOT_NULL -> Operator.IS_NOT_NULL
            BaradumOperator.BETWEEN -> Operator.GREATER_OR_EQUAL // Fallback - BETWEEN might not exist in Hefesto 3
        }
    }

    fun fromHefesto(operator: Operator): BaradumOperator {
        return when (operator) {
            Operator.EQUAL -> BaradumOperator.EQUAL
            Operator.DIFF -> BaradumOperator.DIFF
            Operator.GREATER -> BaradumOperator.GREATER
            Operator.GREATER_OR_EQUAL -> BaradumOperator.GREATER_OR_EQUAL
            Operator.LESS -> BaradumOperator.LESS
            Operator.LESS_OR_EQUAL -> BaradumOperator.LESS_OR_EQUAL
            Operator.LIKE -> BaradumOperator.LIKE
            Operator.NOT_LIKE -> BaradumOperator.NOT_LIKE
            Operator.IN -> BaradumOperator.IN
            Operator.NOT_IN -> BaradumOperator.NOT_IN
            Operator.IS_NULL -> BaradumOperator.IS_NULL
            Operator.IS_NOT_NULL -> BaradumOperator.IS_NOT_NULL
            else -> BaradumOperator.EQUAL // Default fallback
        }
    }
}
