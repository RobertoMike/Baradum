package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.exceptions.FilterException
import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.baradum.requests.FilterRequest
import io.github.robertomike.hefesto.actions.wheres.BaseWhere
import io.github.robertomike.hefesto.actions.wheres.CollectionWhere
import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator

class Filterable {
    val allowedFilters: MutableList<Filter<*>> = ArrayList()

    fun addFilters(vararg filters: String) {
        listOf(*filters).forEach { allowedFilters.add(ExactFilter(it)) }
    }

    fun addFilters(vararg filters: Filter<*>) {
        allowedFilters.addAll(listOf(*filters))
    }

    fun addFilters(filters: Collection<Filter<*>>) {
        allowedFilters.addAll(filters)
    }

    fun apply(builder: Hefesto<*>, request: BasicRequest<*>) {
        allowedFilters.forEach { it.filterByParam(builder, request) }
    }

    fun apply(builder: Hefesto<*>, filters: Collection<FilterRequest>) {
        filters.forEach {
            val where = apply(it) ?: return@forEach
            builder.where(where)
        }
    }

    private fun apply(filter: FilterRequest): BaseWhere? {
        if (filter.subFilters.isEmpty() && filter.field == null) {
            throw FilterException("The field and subFilters cannot be empty at the same time")
        }

        if (filter.subFilters.isNotEmpty()) {
            return CollectionWhere(
                filter.subFilters.mapNotNull(this::apply),
                filter.type
            )
        }

        return searchFilterAndExecute(filter)
    }

    private fun searchFilterAndExecute(filterRequest: FilterRequest): BaseWhere? {
        val filter = allowedFilters
            .firstOrNull { it.param == filterRequest.field }
            ?: throw FilterException("The field '${filterRequest.field}' is not allowed")

        if (!filter.supportBodyOperation()) {
            throw FilterException("The filter '" + filterRequest.javaClass.simpleName + "' not support body request")
        }

        val operator = filterRequest.operator
        val value = filterRequest.value

        if (canApplyIgnore(operator) && (value == null || filter.ignore(value))) {
            return null
        }

        val field = filter.internalName
        val whereOperator = filterRequest.type

        val finalValue: Any? = when (operator) {
            Operator.IN, Operator.NOT_IN -> notNullValue(value, operator).split(",")
                .map(filter::transform)

            Operator.IS_NULL, Operator.IS_NOT_NULL -> null
            else -> filter.transform(notNullValue(value, operator))
        }

        return Where(field, operator, finalValue, whereOperator)
    }

    private fun <T> notNullValue(value: T?, operator: Operator): T {
        return value ?: throw FilterException("The value cannot be null for: $operator")
    }

    private fun canApplyIgnore(operator: Operator): Boolean {
        return !(operator == Operator.IS_NULL || operator == Operator.IS_NOT_NULL)
    }
}
