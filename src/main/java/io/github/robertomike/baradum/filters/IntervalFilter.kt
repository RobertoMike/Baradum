package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator

class IntervalFilter @JvmOverloads constructor(param: String, internalName: String = param) : Filter<Any>(param, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        val start: String
        var end: String? = null
        if (value.contains(",")) {
            val values = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            start = values[0]
            end = values[1]
        } else {
            start = value
        }

        end?.let {
            query.where(
                internalName,
                Operator.LESS_OR_EQUAL,
                it
            )
        }

        if (start.isNotBlank()) {
            query.where(
                internalName,
                Operator.GREATER_OR_EQUAL,
                start
            )
        }
    }
}
