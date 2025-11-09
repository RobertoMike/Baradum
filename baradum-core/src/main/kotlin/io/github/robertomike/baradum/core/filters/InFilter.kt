package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Generic filter for IN operator with comma-separated values.
 * 
 * Usage examples:
 * - "1,2,3" - IN (1,2,3)
 * - "active,pending" - IN ('active','pending')
 * - "USA,UK,CA" - IN ('USA','UK','CA')
 */
open class InFilter @JvmOverloads constructor(
    param: String,
    internalName: String = param,
    private val delimiter: String = ","
) : Filter<List<String>, QueryBuilder<*>>(param, internalName) {

    /**
     * Split the value by delimiter and apply IN operator.
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        val values = transform(value)
        
        if (values.isEmpty()) {
            throw FilterException("Value list cannot be empty for IN filter '$param'")
        }
        
        query.where(internalName, BaradumOperator.IN, values)
    }

    /**
     * Transform comma-separated string into list of trimmed values.
     */
    override fun transform(value: String): List<String> {
        return value.split(delimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
