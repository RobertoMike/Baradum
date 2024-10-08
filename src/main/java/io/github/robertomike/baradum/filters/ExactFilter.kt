package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.builders.Hefesto

class ExactFilter @JvmOverloads constructor(field: String, internalName: String = field) : Filter<Any>(field, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        query.where(internalName, value)
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
