package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Generic SearchFilter for searching across multiple fields using OR conditions.
 * Supports different LIKE strategies and uses OR logic between fields.
 * 
 * Usage:
 * ```kotlin
 * val searchFilter = SearchFilter("search", "username", "email", "firstName")
 * searchFilter.setStrategy(SearchLikeStrategy.COMPLETE) // Default: %value%
 * 
 * // Or use the factory method:
 * SearchFilter.of("username", "email") // defaults param to "search"
 * ```
 */
open class SearchFilter @JvmOverloads constructor(
    param: String,
    vararg fields: String
) : Filter<Any, QueryBuilder<*>>(param, "") {

    private var internalNames: List<String> = fields.toList()
    private var strategy: SearchLikeStrategy = SearchLikeStrategy.COMPLETE

    companion object {
        /**
         * Factory method for creating a SearchFilter with default "search" parameter.
         * Convenient for Java usage.
         */
        @JvmStatic
        fun  of(vararg fields: String): SearchFilter {
            return SearchFilter("search", *fields)
        }
    }

    open fun setInternalNames(names: List<String>): SearchFilter {
        this.internalNames = names
        return this
    }

    open fun setStrategy(strategy: SearchLikeStrategy): SearchFilter {
        this.strategy = strategy
        return this
    }

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        if (internalNames.isEmpty()) return

        val likeValue = strategy.apply(value)
        
        // Use WhereOperator.OR for all search fields except the first
        internalNames.forEachIndexed { index, field ->
            val whereOp = if (index == 0) WhereOperator.AND else WhereOperator.OR
            query.where(field, BaradumOperator.LIKE, likeValue, whereOp)
        }
    }
}
