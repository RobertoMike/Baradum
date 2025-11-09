package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Generic filter for NULL/NOT NULL checks.
 * 
 * Usage examples:
 * - "null" or "true" - IS NULL
 * - "false" or "not_null" - IS NOT NULL
 * 
 * @param Q QueryBuilder type
 */
open class IsNullFilter<Q : QueryBuilder<*>> @JvmOverloads constructor(
    param: String,
    internalName: String = param
) : Filter<Boolean, Q>(param, internalName) {

    /**
     * Apply NULL or NOT NULL condition based on the value.
     */
    override fun filterByParam(query: Q, value: String) {
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
