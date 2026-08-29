package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic IntervalFilter for filtering numeric ranges.
 * Expects format: "min-max" (e.g., "10-50", "0-100")
 * Also supports comma format: "min,max" for backward compatibility
 * Supports single value: "50" for exact match
 *
 * Usage:
 * ```kotlin
 * IntervalFilter("age") // Incoming: "18-65" -> WHERE age >= 18 AND age <= 65
 * IntervalFilter("age") // Incoming: "18,65" -> WHERE age >= 18 AND age <= 65 (backward compat)
 * IntervalFilter("price") // Incoming: "100" -> WHERE price = 100
 * IntervalFilter("temperature") // Incoming: "-10--5" -> WHERE temperature >= -10 AND temperature <= -5
 * ```
 */
open class IntervalFilter : Filter<Any, QueryBuilder<*>> {

    @JvmOverloads
    constructor(param: String, internalName: String = param) : super(param, internalName)

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)

    companion object {
        /**
         * Factory method for creating IntervalFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): IntervalFilter = IntervalFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): IntervalFilter = IntervalFilter(property, param)

        // Matches "min-max" where either bound is optional, may itself be negative, and may be
        // padded with whitespace (e.g. "18-65", "18-", "-65", "-10--5", " 18 - 65 "). A bare
        // value with no "-" at all (or a single negative number like "-5") does not match and
        // falls through to EQUAL.
        private val RANGE_PATTERN = Regex("""^\s*(-?\d+(?:\.\d+)?)?\s*-\s*(-?\d+(?:\.\d+)?)?\s*$""")
    }

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        // Normalize comma to hyphen for backward compatibility
        val normalizedValue = if (value.contains(",")) value.replace(",", "-") else value

        val rangeMatch = RANGE_PATTERN.matchEntire(normalizedValue)
        if (rangeMatch == null) {
            // Single value - exact match. Note: a bare negative number such as "-5" still
            // matches RANGE_PATTERN (as a "max only" bound, for backward compatibility) rather
            // than landing here - that ambiguity is inherent to the dash-based syntax. Use
            // ComparisonFilter or ExactFilter if you need an unambiguous exact negative match.
            query.where(internalName, BaradumOperator.EQUAL, normalizedValue.trim())
            return
        }

        val (min, max) = rangeMatch.destructured
        if (min.isNotEmpty()) {
            query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, min)
        }
        if (max.isNotEmpty()) {
            query.where(internalName, BaradumOperator.LESS_OR_EQUAL, max)
        }
    }
}
