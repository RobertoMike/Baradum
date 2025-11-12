package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel
import java.util.function.BiConsumer

class CustomFilter(
    param: String, 
    private val consumer: BiConsumer<HefestoQueryBuilder<out BaseModel>, String>
) : Filter<Any, HefestoQueryBuilder<out BaseModel>>(param, param) {
    
    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        consumer.accept(query, value)
    }
}
