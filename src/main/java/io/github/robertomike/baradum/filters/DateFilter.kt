package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.exceptions.FilterException
import io.github.robertomike.hefesto.builders.Hefesto
import java.text.SimpleDateFormat
import java.util.*

class DateFilter @JvmOverloads constructor(field: String, internalName: String = field) : Filter<Date>(field, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        val operator = getOperator(value)
        val finalValue = cleanValue(value)

        val date: Date = transform(finalValue)

        query.where(
            internalName,
            operator,
            date
        )
    }

    override fun transform(value: String): Date {
        try {
            return format.parse(value)
        } catch (e: Exception) {
            throw FilterException("invalid dates")
        }
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }

    companion object {
        private var format = SimpleDateFormat("yyyy-MM-dd")

        @JvmStatic
        fun setFormat(format: String) {
            Companion.format = SimpleDateFormat(format)
        }
    }
}
