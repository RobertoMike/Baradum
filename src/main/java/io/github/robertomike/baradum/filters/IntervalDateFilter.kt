package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.exceptions.FilterException
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator
import java.text.SimpleDateFormat
import java.util.*

class IntervalDateFilter @JvmOverloads constructor(field: String, internalName: String = field) : Filter<Date>(field, internalName) {
    override fun filterByParam(query: Hefesto<*>, value: String) {
        lateinit var startDate: String
        var endDate: Date? = null
        if (value.contains(",")) {
            val dates = value.split(",")
            startDate = dates[0]
            endDate = transform(dates[1])
        } else {
            startDate = value
        }

        endDate?.let {
            query.where(
                internalName,
                Operator.LESS_OR_EQUAL,
                it
            )
        }

        if (startDate.isNotBlank()) {
            query.where(
                internalName,
                Operator.GREATER_OR_EQUAL,
                transform(startDate)
            )
        }
    }

    override fun transform(value: String): Date {
        try {
            return format.parse(value)
        } catch (e: Exception) {
            throw FilterException("invalid dates")
        }
    }

    companion object {
        private var format = SimpleDateFormat("yyyy-MM-dd")

        @JvmStatic
        fun setFormat(format: String) {
            Companion.format = SimpleDateFormat(format)
        }
    }
}
