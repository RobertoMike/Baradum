package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.WhereOperator

class EmptyFilter @JvmOverloads constructor(param: String, internalName: String = param) : Filter<Any>(param, internalName) {
    override fun filterByParam(query: Hefesto<*>, request: BasicRequest<*>) {
        if (request.findByName(param) != null) {
            filterByParam(query, "")
        }
    }

    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(
            Where(internalName, Operator.IS_NULL, null),
            Where.make(internalName, "", WhereOperator.OR)
        )
    }
}
