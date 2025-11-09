package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel

class ExactFilter @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Any, HefestoQueryBuilder<out BaseModel>>(param, internalName) {
    
    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        // Convert string value to appropriate type for simple cases
        val convertedValue: Any = when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            // Only convert if it's clearly a number (no letters)
            value.matches(Regex("^-?\\d+$")) && value.length < 10 -> value.toInt()
            value.matches(Regex("^-?\\d+$")) -> value.toLong()
            value.matches(Regex("^-?\\d+\\.\\d+$")) -> value.toDouble()
            else -> value // Keep as string - Hibernate will handle enum conversion
        }
        query.where(internalName, BaradumOperator.EQUAL, convertedValue)
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
