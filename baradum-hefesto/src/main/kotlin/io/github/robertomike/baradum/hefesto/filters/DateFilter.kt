package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.hefesto.models.BaseModel
import java.text.SimpleDateFormat
import java.util.*

class DateFilter @JvmOverloads constructor(
    param: String, 
    internalName: String = param
) : Filter<Date, HefestoQueryBuilder<out BaseModel>>(param, internalName) {
    
    override fun filterByParam(query: HefestoQueryBuilder<out BaseModel>, value: String) {
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
