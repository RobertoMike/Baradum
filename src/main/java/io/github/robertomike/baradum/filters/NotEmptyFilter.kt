package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator

class NotEmptyFilter @JvmOverloads constructor(param: String, internalName: String = param) : Filter<Any>(param, internalName) {
    override fun filterByParam(query: Hefesto<*>, request: BasicRequest<*>) {
        if (request.findByName(param) != null) {
            filterByParam(query, "")
        }
    }

    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(
            Where(internalName, Operator.IS_NOT_NULL, null),
            Where(internalName, Operator.DIFF, "")
        )
    }
}
