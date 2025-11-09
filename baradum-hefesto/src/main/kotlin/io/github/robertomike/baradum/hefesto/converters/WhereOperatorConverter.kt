package io.github.robertomike.baradum.hefesto.converters

import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.hefesto.enums.WhereOperator as HefestoWhereOperator

object WhereOperatorConverter {
    fun toHefesto(operator: WhereOperator): HefestoWhereOperator {
        return when (operator) {
            WhereOperator.AND -> HefestoWhereOperator.AND
            WhereOperator.OR -> HefestoWhereOperator.OR
        }
    }

    fun fromHefesto(operator: HefestoWhereOperator): WhereOperator {
        return when (operator) {
            HefestoWhereOperator.AND -> WhereOperator.AND
            HefestoWhereOperator.OR -> WhereOperator.OR
        }
    }
}
