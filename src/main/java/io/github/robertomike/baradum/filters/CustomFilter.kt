package io.github.robertomike.baradum.filters

import io.github.robertomike.hefesto.builders.Hefesto
import java.util.function.BiConsumer

class CustomFilter(field: String, private val consumer: BiConsumer<Hefesto<*>, String>) : Filter<Any>(field, field) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        consumer.accept(query, value)
    }
}
