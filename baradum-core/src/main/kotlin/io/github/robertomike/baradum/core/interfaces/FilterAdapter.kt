package io.github.robertomike.baradum.core.interfaces

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.WhereOperator

/**
 * Interface for applying filters to a query builder
 */
interface FilterAdapter<Q : QueryBuilder<*>> {
    /**
     * Apply a filter condition to the query builder
     */
    fun apply(builder: Q, field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator = WhereOperator.AND)
}
