package io.github.robertomike.baradum.requests

import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.WhereOperator

class FilterRequest {
    var field: String? = null
    var value: String? = null
    var operator = Operator.EQUAL
    var type = WhereOperator.AND
    var subFilters: List<FilterRequest> = ArrayList()
}
