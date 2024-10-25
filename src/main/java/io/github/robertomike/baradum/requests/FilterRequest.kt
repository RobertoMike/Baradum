package io.github.robertomike.baradum.requests

import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.WhereOperator

data class FilterRequest @JvmOverloads constructor(
    var field: String? = null,
    var value: String? = null,
    var operator: Operator = Operator.EQUAL,
    var type: WhereOperator = WhereOperator.AND,
    var subFilters: List<FilterRequest> = ArrayList()
)
