package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic filter for comparison operations.
 * Supports: GREATER (>), LESS (<), GREATER_OR_EQUAL (>=), LESS_OR_EQUAL (<=), DIFF (!=)
 *
 * Usage examples:
 * - ">25" - Greater than 25
 * - ">=18" - Greater than or equal to 18
 * - "<100" - Less than 100
 * - "<=65" - Less than or equal to 65
 * - "!=0" - Not equal to 0
 * - "25" - Defaults to EQUAL if no operator prefix
 *
 * @param Q QueryBuilder type
 */
open class ComparisonFilter : Filter<String, QueryBuilder<*>> {

    @JvmOverloads
    constructor(param: String, internalName: String = param) : super(param, internalName)

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)

    companion object {
        /**
         * Factory method for creating ComparisonFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): ComparisonFilter = ComparisonFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): ComparisonFilter = ComparisonFilter(property, param)
    }

    /**
     * Parse the value and apply the appropriate comparison operator.
     * Supports prefixes: >, >=, <, <=, !=
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        val (operator, cleanValue) = parseOperatorAndValue(value)

        if (cleanValue.isEmpty()) {
            throw FilterException("Value cannot be empty for comparison filter '$param'")
        }

        query.where(internalName, operator, cleanValue)
    }

    /**
     * Parse the operator prefix and extract the clean value.
     * Returns a pair of (operator, value)
     */
    protected open fun parseOperatorAndValue(value: String): Pair<BaradumOperator, String> {
        return when {
            value.startsWith(">=") -> BaradumOperator.GREATER_OR_EQUAL to value.substring(2).trim()
            value.startsWith("<=") -> BaradumOperator.LESS_OR_EQUAL to value.substring(2).trim()
            value.startsWith("!=") -> BaradumOperator.DIFF to value.substring(2).trim()
            value.startsWith(">") -> BaradumOperator.GREATER to value.substring(1).trim()
            value.startsWith("<") -> BaradumOperator.LESS to value.substring(1).trim()
            else -> BaradumOperator.EQUAL to value.trim()
        }
    }

    override fun transform(value: String): String = value
}
