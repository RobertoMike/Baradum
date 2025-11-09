package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.requests.FilterRequest

class Filterable<Q : QueryBuilder<*>> {
    val allowedFilters: MutableList<Filter<*, Q>> = ArrayList()

    fun addFilters(vararg filters: Filter<*, *>) {
        allowedFilters.addAll(listOf(*filters) as Collection<Filter<*, Q>>)
    }

    fun addFilters(filters: Collection<Filter<*, *>>) {
        allowedFilters.addAll(filters as Collection<Filter<*, Q>>)
    }

    fun apply(builder: Q, request: BasicRequest<*>) {
        allowedFilters.forEach { it.filterByParam(builder, request) }
    }

    fun apply(builder: Q, params: Map<String, String>) {
        allowedFilters.forEach { filter ->
            val value = params[filter.param]
            if (value != null) {
                filter.filterByParam(builder, value)
            }
        }
    }

    fun apply(builder: Q, filters: Collection<FilterRequest>) {
        filters.forEach {
            apply(builder, it)
        }
    }

    private fun apply(builder: Q, filter: FilterRequest) {
        if (filter.subFilters.isEmpty() && filter.field == null) {
            throw FilterException("The field and subFilters cannot be empty at the same time")
        }

        if (filter.subFilters.isNotEmpty()) {
            // For nested filters, apply recursively
            filter.subFilters.forEach { subFilter ->
                apply(builder, subFilter)
            }
            return
        }

        applySimpleFilter(builder, filter)
    }

    private fun applySimpleFilter(builder: Q, filterRequest: FilterRequest) {
        val filterDef = allowedFilters
            .firstOrNull { it.param == filterRequest.field }
            ?: throw FilterException("The field '${filterRequest.field}' is not allowed")

        if (!filterDef.supportBodyOperation()) {
            throw FilterException("The filter '${filterDef.javaClass.simpleName}' does not support body request")
        }

        val operator = filterRequest.operator
        val value = filterRequest.value

        if (canApplyIgnore(operator) && (value == null || filterDef.ignore(value))) {
            return
        }

        val field = filterDef.internalName
        val whereOperator = filterRequest.type

        val finalValue: Any? = when (operator) {
            BaradumOperator.IN, BaradumOperator.NOT_IN -> notNullValue(value, operator).split(",")
                .map(filterDef::transform)

            BaradumOperator.IS_NULL, BaradumOperator.IS_NOT_NULL -> null
            else -> filterDef.transform(notNullValue(value, operator))
        }

        builder.where(field, operator, finalValue, whereOperator)
    }

    private fun <T> notNullValue(value: T?, operator: BaradumOperator): T {
        return value ?: throw FilterException("The value cannot be null for: $operator")
    }

    private fun canApplyIgnore(operator: BaradumOperator): Boolean {
        return !(operator == BaradumOperator.IS_NULL || operator == BaradumOperator.IS_NOT_NULL)
    }
}
