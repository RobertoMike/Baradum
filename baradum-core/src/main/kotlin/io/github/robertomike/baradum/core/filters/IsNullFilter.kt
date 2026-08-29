package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic filter for NULL/NOT NULL checks.
 *
 * Usage examples:
 * - "null" or "true" - IS NULL
 * - "false" or "not_null" - IS NOT NULL
 *
 * @param Q QueryBuilder type
 */
open class IsNullFilter : Filter<Boolean, QueryBuilder<*>> {

    @JvmOverloads
    constructor(param: String, internalName: String = param) : super(param, internalName)

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)

    companion object {
        /**
         * Factory method for creating IsNullFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): IsNullFilter = IsNullFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): IsNullFilter = IsNullFilter(property, param)
    }

    /**
     * Apply NULL or NOT NULL condition based on the value.
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        val isNull = transform(value)

        if (isNull) {
            query.where(internalName, BaradumOperator.IS_NULL, null)
        } else {
            query.where(internalName, BaradumOperator.IS_NOT_NULL, null)
        }
    }

    /**
     * Transform string value to boolean.
     * Accepts: "null", "true", "1", "yes" for NULL
     * Accepts: "not_null", "false", "0", "no" for NOT NULL
     */
    override fun transform(value: String): Boolean {
        return when (value.lowercase().trim()) {
            "null", "true", "1", "yes" -> true
            "not_null", "false", "0", "no" -> false
            else -> throw FilterException("Invalid value for IsNullFilter '$param'. Use 'null' or 'not_null'")
        }
    }
}
