package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Filter for "greater than" comparisons.
 * 
 * Supports two modes:
 * - GREATER: Strictly greater than (>)
 * - GREATER_OR_EQUAL: Greater than or equal to (>=)
 * 
 * Works with any comparable type: numbers, dates, strings, etc.
 * 
 * Usage examples:
 * ```kotlin
 * // Traditional string-based
 * GreaterFilter("age")  // age > value
 * GreaterFilter("age", orEqual = true)  // age >= value
 * GreaterFilter("minAge", "age", orEqual = true)  // age >= minAge
 * 
 * // Type-safe with KProperty
 * GreaterFilter(User::age)  // age > value
 * GreaterFilter(User::age, orEqual = true)  // age >= value
 * GreaterFilter(User::age, "minAge", orEqual = true)  // Custom param name
 * 
 * // For dates (with DateFilter for parsing)
 * GreaterFilter("createdAfter", "created_at")  // created_at > date
 * ```
 * 
 * @param param The request parameter name to read from
 * @param internalName The database field name (defaults to param)
 * @param orEqual If true, uses >= instead of > (default: false)
 */
open class GreaterFilter : Filter<String, QueryBuilder<*>> {

    private val orEqual: Boolean

    @JvmOverloads
    constructor(
        param: String,
        internalName: String = param,
        orEqual: Boolean = false
    ) : super(param, internalName) {
        this.orEqual = orEqual
    }

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(
        property: KProperty1<*, *>,
        param: String? = null,
        orEqual: Boolean = false
    ) : super(property, param) {
        this.orEqual = orEqual
    }

    companion object {
        /**
         * Factory methods for creating GreaterFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): GreaterFilter = GreaterFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, orEqual: Boolean): GreaterFilter = 
            GreaterFilter(property, null, orEqual)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String, orEqual: Boolean): GreaterFilter = 
            GreaterFilter(property, param, orEqual)
    }

    /**
     * Apply the greater than filter to the query
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        if (value.isBlank()) {
            throw FilterException("Value cannot be empty for greater filter '$param'")
        }

        val operator = if (orEqual) {
            BaradumOperator.GREATER_OR_EQUAL
        } else {
            BaradumOperator.GREATER
        }

        // Try to parse as number for better type safety
        val parsedValue = tryParseNumber(value) ?: value
        
        query.where(internalName, operator, parsedValue)
    }

    /**
     * Try to parse the value as a number (Int, Long, Double) for type safety.
     * Returns null if parsing fails, falling back to string comparison.
     */
    private fun tryParseNumber(value: String): Any? {
        return try {
            when {
                value.contains(".") -> value.toDouble()
                value.toLongOrNull()?.let { it > Int.MAX_VALUE || it < Int.MIN_VALUE } == true -> value.toLong()
                else -> value.toInt()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun transform(value: String): String = value
}
