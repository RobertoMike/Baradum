package io.github.robertomike.baradum.core.sorting

import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.exceptions.SortableException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.requests.OrderRequest
import java.util.*

class Sortable<Q : QueryBuilder<*>> {
    private var allowedSorts: MutableList<OrderBy> = ArrayList()

    fun addSorts(vararg sorts: String) {
        listOf(*sorts).forEach { allowedSorts.add(OrderBy(it)) }
    }

    fun addSorts(vararg sorts: OrderBy) {
        allowedSorts.addAll(listOf(*sorts))
    }

    fun addSorts(sorts: List<OrderBy>) {
        allowedSorts.addAll(sorts)
    }

    fun apply(builder: Q, request: BasicRequest<*>) {
        if (request.notExistsByName("sort")) {
            return
        }

        val sorts = request.findByName("sort")!!
            .trim { it <= ' ' }
            .split(",")

        val sortList = sorts.map {
            OrderRequest(
                it.replace("-", ""),
                if (it.contains("-")) SortDirection.DESC else SortDirection.ASC
            )
        }

        apply(builder, sortList)
    }

    fun apply(builder: Q, params: Map<String, String>) {
        val sortParam = params["sort"] ?: return

        val sorts = sortParam
            .trim()
            .split(",")

        val sortList = sorts.map {
            OrderRequest(
                it.replace("-", ""),
                if (it.contains("-")) SortDirection.DESC else SortDirection.ASC
            )
        }

        apply(builder, sortList)
    }

    fun apply(builder: Q, sorts: List<OrderRequest>) {
        sorts.forEach { sort ->
            if (sort.field == null) {
                throw SortableException("The sort list is not valid, one element must have a field null")
            }

            val result = allowedSorts.firstOrNull { allowedSort -> allowedSort.name == sort.field }

            if (result == null) {
                throw SortableException("The field '${sort.field}' is not valid")
            }

            builder.orderBy(result.internalName, sort.sort)
        }
    }
}
