package io.github.robertomike.baradum.core.requests

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.WhereOperator

data class FilterRequest @JvmOverloads constructor(
    var field: String? = null,
    var value: String? = null,
    var operator: BaradumOperator = BaradumOperator.EQUAL,
    var type: WhereOperator = WhereOperator.AND,
    var subFilters: List<FilterRequest> = ArrayList()
)
