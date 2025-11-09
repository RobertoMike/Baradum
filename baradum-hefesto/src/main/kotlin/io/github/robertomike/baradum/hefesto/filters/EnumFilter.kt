package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.core.utils.valueOf
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel

class EnumFilter<T : Enum<T>> @JvmOverloads constructor(
    param: String,
    internalName: String = param,
    private val classEnum: Class<T>,
) : Filter<T, HefestoQueryBuilder<out BaseModel>>(param, internalName) {

    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
        if (value.contains(",")) {
            val values = value.split(",")
                .map(this::transform)
                .toSet()

            query.where(internalName, BaradumOperator.IN, values)
            return
        }

        query.where(internalName, BaradumOperator.EQUAL, transform(value))
    }

    override fun transform(value: String): T {
        try {
            return classEnum.valueOf(value)
        } catch (e: Exception) {
            val allowed = classEnum.getEnumConstants().joinToString { it.name }
            throw FilterException("Invalid value for $param, allowed values: $allowed")
        }
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
