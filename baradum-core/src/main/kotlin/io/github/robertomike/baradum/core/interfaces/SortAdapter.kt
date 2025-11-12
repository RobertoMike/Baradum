package io.github.robertomike.baradum.core.interfaces

import io.github.robertomike.baradum.core.enums.SortDirection

/**
 * Interface for applying sorting to a query builder
 */
interface SortAdapter<Q : QueryBuilder<*>> {
    /**
     * Apply a sort to the query builder
     */
    fun apply(builder: Q, field: String, direction: SortDirection)
}
