package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator

class PartialFilter @JvmOverloads constructor(field: String, internalName: String = field) : Filter<Any>(field, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(internalName, Operator.LIKE, "$value%")
    }
}
