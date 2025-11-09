package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Generic ExactFilter for filtering by exact value match.
 * Automatically converts String values to appropriate types (Boolean, Int, Long, Double).
 * 
 * Usage:
 * ```kotlin
 * ExactFilter("country")
 * ExactFilter("status") // Works with enums
 * ExactFilter("isActive") // Converts "true"/"false" to Boolean
 * ExactFilter("age") // Converts to appropriate numeric type
 * ```
 */
open class ExactFilter<Q : QueryBuilder<*>> @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Any, Q>(param, internalName) {
    
    override fun filterByParam(query: Q, value: String) {
        // Convert string value to appropriate type for simple cases
        val convertedValue: Any = when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            // Only convert if it's clearly a number (no letters)
            value.matches(Regex("^-?\\d+$")) && value.length < 10 -> value.toInt()
            value.matches(Regex("^-?\\d+$")) -> value.toLong()
            value.matches(Regex("^-?\\d+\\.\\d+$")) -> value.toDouble()
            else -> value // Keep as string - ORM will handle enum conversion
        }
        query.where(internalName, BaradumOperator.EQUAL, convertedValue)
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
