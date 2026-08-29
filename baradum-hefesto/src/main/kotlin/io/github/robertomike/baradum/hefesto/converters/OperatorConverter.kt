package io.github.robertomike.baradum.hefesto.converters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.BaradumException
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
            // Hefesto's own Operator enum has neither of these - HefestoQueryBuilder.where()
            // special-cases both before ever calling this converter (via whereCustom/orWhereCustom
            // + raw CriteriaBuilder predicates). Reaching here means that special-casing was
            // bypassed somehow, so fail loudly instead of silently building the wrong query
            // (this used to fall back to GREATER_OR_EQUAL for BETWEEN, silently dropping the
            // upper bound).
            BaradumOperator.BETWEEN -> throw BaradumException("BETWEEN is not a native Hefesto operator and must be handled by HefestoQueryBuilder before reaching OperatorConverter")
            BaradumOperator.LIKE_IGNORE_CASE -> throw BaradumException("LIKE_IGNORE_CASE is not a native Hefesto operator and must be handled by HefestoQueryBuilder before reaching OperatorConverter")
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
