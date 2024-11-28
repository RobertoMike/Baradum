package io.github.robertomike.baradum.sorting

import io.github.robertomike.baradum.exceptions.SortableException
import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.baradum.requests.OrderRequest
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Sort
import java.util.*

class Sortable {
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

    fun apply(builder: Hefesto<*>, request: BasicRequest<*>) {
        if (request.notExistsByName("sort")) {
            return
        }

        val sorts = request.findByName("sort")!!
            .trim { it <= ' ' }
            .split(",")

        val sortList = sorts.map {
            OrderRequest(
                it.replace("-", ""),
                if (it.contains("-")) Sort.DESC else Sort.ASC
            )
        }

        apply(builder, sortList)
    }

    fun apply(builder: Hefesto<*>, sorts: List<OrderRequest>) {
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
