package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Filter for "less than" comparisons.
 * 
 * Supports two modes:
 * - LESS: Strictly less than (<)
 * - LESS_OR_EQUAL: Less than or equal to (<=)
 * 
 * Works with any comparable type: numbers, dates, strings, etc.
 * 
 * Usage examples:
 * ```kotlin
 * // Traditional string-based
 * LessFilter("age")  // age < value
 * LessFilter("age", orEqual = true)  // age <= value
 * LessFilter("maxAge", "age", orEqual = true)  // age <= maxAge
 * 
 * // Type-safe with KProperty
 * LessFilter(User::age)  // age < value
 * LessFilter(User::age, orEqual = true)  // age <= value
 * LessFilter(User::age, "maxAge", orEqual = true)  // Custom param name
 * 
 * // For dates (with DateFilter for parsing)
 * LessFilter("createdBefore", "created_at")  // created_at < date
 * ```
 * 
 * @param param The request parameter name to read from
 * @param internalName The database field name (defaults to param)
 * @param orEqual If true, uses <= instead of < (default: false)
 */
open class LessFilter : Filter<String, QueryBuilder<*>> {

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
         * Factory methods for creating LessFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): LessFilter = LessFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, orEqual: Boolean): LessFilter = 
            LessFilter(property, null, orEqual)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String, orEqual: Boolean): LessFilter = 
            LessFilter(property, param, orEqual)
    }

    /**
     * Apply the less than filter to the query
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        if (value.isBlank()) {
            throw FilterException("Value cannot be empty for less filter '$param'")
        }

        val operator = if (orEqual) {
            BaradumOperator.LESS_OR_EQUAL
        } else {
            BaradumOperator.LESS
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
