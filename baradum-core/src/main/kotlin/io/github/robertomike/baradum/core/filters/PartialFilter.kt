package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic PartialFilter for filtering using LIKE operator with wildcards.
 * Supports different strategies for wildcard placement.
 * 
 * Usage examples:
 * ```kotlin
 * // Traditional string-based
 * PartialFilter("username") // Default: FINAL strategy (value%)
 * PartialFilter("email").setStrategy(SearchLikeStrategy.COMPLETE) // (%value%)
 * 
 * // Type-safe with KProperty
 * PartialFilter(User::username)
 * PartialFilter(User::email, "searchEmail").setStrategy(SearchLikeStrategy.COMPLETE)
 * 
 * // User can also provide wildcards: "%JOHN%" will be used as-is
 * ```
 */
open class PartialFilter : Filter<Any, QueryBuilder<*>> {

    @JvmOverloads
    constructor(param: String, internalName: String = param) : super(param, internalName)

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)

    companion object {
        /**
         * Factory method for creating PartialFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): PartialFilter = PartialFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): PartialFilter = PartialFilter(property, param)
    }

    private var strategy: SearchLikeStrategy = SearchLikeStrategy.FINAL
    private var ignoreCase: Boolean = false

    open fun setStrategy(strategy: SearchLikeStrategy): PartialFilter {
        this.strategy = strategy
        return this
    }

    /**
     * When true, matches case-insensitively (e.g. "JOHN" matches "john").
     * Implemented via the database's own LOWER()/case-insensitive LIKE support, so the exact
     * case-folding behavior follows the underlying column's collation.
     */
    open fun setIgnoreCase(ignoreCase: Boolean): PartialFilter {
        this.ignoreCase = ignoreCase
        return this
    }

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        // If value already contains %, use it as-is; otherwise apply strategy
        val likeValue = if (value.contains("%")) value else strategy.apply(value)
        val operator = if (ignoreCase) BaradumOperator.LIKE_IGNORE_CASE else BaradumOperator.LIKE
        query.where(internalName, operator, likeValue)
    }
}
