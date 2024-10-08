package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.exceptions.FilterException
import io.github.robertomike.baradum.utils.valueOf
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator

class EnumFilter<T : Enum<T>>
@JvmOverloads constructor(
    field: String,
    internalName: String = field,
    private val classEnum: Class<T>,
) : Filter<T>(field, internalName) {

    override fun filterByParam(query: Hefesto<*>, value: String) {
        if (value.contains(",")) {
            val values = value.split(",")
                .map(this::transform)
                .toSet()

            query.where(internalName, Operator.IN, values)
            return
        }

        query.where(internalName, transform(value))
    }

    override fun transform(value: String): T {
        try {
            return classEnum.valueOf(value)
        } catch (e: Exception) {
            val allowed = classEnum.getEnumConstants().joinToString { it.name }
            throw FilterException("Invalid value for $field, allowed values: $allowed")
        }
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
