package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.builders.Hefesto

class ExactFilter @JvmOverloads constructor(param: String, internalName: String = param) : Filter<Any>(param, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(internalName, value)
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
