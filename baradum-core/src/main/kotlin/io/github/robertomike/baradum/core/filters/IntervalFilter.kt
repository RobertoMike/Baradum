package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

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
 * ```
 */
open class IntervalFilter @JvmOverloads constructor(
    param: String,
    internalName: String = param
) : Filter<Any, QueryBuilder<*>>(param, internalName) {

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        // Normalize comma to hyphen for backward compatibility
        val normalizedValue = if (value.contains(",")) value.replace(",", "-") else value
        
        if (!normalizedValue.contains("-")) {
            // Single value - exact match
            query.where(internalName, BaradumOperator.EQUAL, normalizedValue)
            return
        }

        val parts = normalizedValue.split("-")
        if (parts.size >= 2) {
            val min = parts[0].trim()
            val max = parts[1].trim()
            
            if (min.isNotEmpty()) {
                query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, min)
            }
            if (max.isNotEmpty()) {
                query.where(internalName, BaradumOperator.LESS_OR_EQUAL, max)
            }
        }
    }
}
