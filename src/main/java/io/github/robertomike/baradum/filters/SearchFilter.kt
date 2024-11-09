package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.WhereOperator

class SearchFilter(param: String, vararg fields: String) : Filter<Any>(
    param, param
) {
    private val fields = setOf(*fields)

    companion object {
        @JvmStatic
        fun of(vararg fields: String) = SearchFilter("search", *fields)
    }

    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(
            fields.map { Where(it, Operator.LIKE, "$value%", WhereOperator.OR) }
        )
    }
}
