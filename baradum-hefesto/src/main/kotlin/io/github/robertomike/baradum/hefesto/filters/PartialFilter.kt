package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel

class PartialFilter @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Any, HefestoQueryBuilder<out BaseModel>>(param, internalName) {

    private var strategy: SearchLikeStrategy = SearchLikeStrategy.FINAL

    fun setStrategy(strategy: SearchLikeStrategy): PartialFilter {
        this.strategy = strategy
        return this
    }

    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        // If value already contains %, use it as-is; otherwise apply strategy
        val likeValue = if (value.contains("%")) value else strategy.apply(value)
        query.where(internalName, BaradumOperator.LIKE, likeValue)
    }
}
