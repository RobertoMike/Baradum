package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel

class IntervalFilter @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Any, HefestoQueryBuilder<out BaseModel>>(param, internalName) {
    
    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        val start: String
        var end: String? = null
        if (value.contains(",")) {
            val values = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            start = if (values.isNotEmpty()) values[0] else ""
            end = if (values.size > 1) values[1] else null
        } else {
            start = value
        }

        end?.let {
            if (it.isNotBlank()) {
                query.where(
                    internalName,
                    BaradumOperator.LESS_OR_EQUAL,
                    it
                )
            }
        }

        if (start.isNotBlank()) {
            query.where(
                internalName,
                BaradumOperator.GREATER_OR_EQUAL,
                start
            )
        }
    }
}
