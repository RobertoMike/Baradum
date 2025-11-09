package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Generic PartialFilter for filtering using LIKE operator with wildcards.
 * Supports different strategies for wildcard placement.
 * 
 * Usage:
 * ```kotlin
 * PartialFilter("username") // Default: FINAL strategy (value%)
 * PartialFilter("email").setStrategy(SearchLikeStrategy.COMPLETE) // (%value%)
 * // User can also provide wildcards: "%JOHN%" will be used as-is
 * ```
 */
open class PartialFilter<Q : QueryBuilder<*>> @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Any, Q>(param, internalName) {

    private var strategy: SearchLikeStrategy = SearchLikeStrategy.FINAL

    open fun setStrategy(strategy: SearchLikeStrategy): PartialFilter<Q> {
        this.strategy = strategy
        return this
    }

    override fun filterByParam(query: Q, value: String) {
        // If value already contains %, use it as-is; otherwise apply strategy
        val likeValue = if (value.contains("%")) value else strategy.apply(value)
        query.where(internalName, BaradumOperator.LIKE, likeValue)
    }
}
