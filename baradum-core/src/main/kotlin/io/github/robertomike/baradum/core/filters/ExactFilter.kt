package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic ExactFilter for filtering by exact value match.
 * Automatically converts String values to appropriate types (Boolean, Int, Long, Double).
 * 
 * Usage examples:
 * ```kotlin
 * // Traditional string-based
 * ExactFilter("country")
 * ExactFilter("status") // Works with enums
 * ExactFilter("isActive") // Converts "true"/"false" to Boolean
 * ExactFilter("age") // Converts to appropriate numeric type
 * 
 * // Type-safe with KProperty
 * ExactFilter(User::country)
 * ExactFilter(User::status)
 * ExactFilter(User::isActive, "active")  // Custom param name
 * ```
 */
open class ExactFilter : Filter<Any, QueryBuilder<*>> {

    @JvmOverloads
    constructor(param: String, internalName: String = param) : super(param, internalName)

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)

    companion object {
        /**
         * Factory method for creating ExactFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): ExactFilter = ExactFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): ExactFilter = ExactFilter(property, param)
    }
    
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
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
