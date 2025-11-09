package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel

class SearchFilter(param: String, vararg fields: String) : Filter<Any, HefestoQueryBuilder<out BaseModel>>(
    param, param
) {
    private val fields = setOf(*fields)
    private var strategy: SearchLikeStrategy = SearchLikeStrategy.COMPLETE

    companion object {
        @JvmStatic
        fun of(vararg fields: String) = SearchFilter("search", *fields)
    }

    fun setStrategy(strategy: SearchLikeStrategy): SearchFilter {
        this.strategy = strategy
        return this
    }

    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        fields.forEachIndexed { index, field ->
            if (index == 0) {
                query.where(field, BaradumOperator.LIKE, strategy.apply(value), WhereOperator.AND)
            } else {
                query.where(field, BaradumOperator.LIKE, strategy.apply(value), WhereOperator.OR)
            }
        }
    }
}
