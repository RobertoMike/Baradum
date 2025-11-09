package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Generic DateFilter for filtering date and datetime fields.
 * Supports multiple formats:
 * - Range with pipe: "2024-01-01|2024-12-31" -> BETWEEN dates
 * - With operators: ">2024-01-01", "<=2024-12-31", etc.
 * - Single date: "2024-01-01" -> exact match
 * - Supports both java.util.Date and java.time.LocalDate/LocalDateTime
 * 
 * Usage:
 * ```kotlin
 * DateFilter("createdAt") // Incoming: "2024-01-01|2024-12-31"
 * DateFilter("birthDate") // Incoming: ">1990-05-15"
 * DateFilter.setDateFormat("dd/MM/yyyy") // Custom format
 * ```
 */
open class DateFilter<Q : QueryBuilder<*>> @JvmOverloads constructor(
    param: String,
    internalName: String = param
) : Filter<Any, Q>(param, internalName) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    companion object {
        private var dateFormat = SimpleDateFormat("yyyy-MM-dd")

        /**
         * Set custom date format for java.util.Date parsing.
         * Default: "yyyy-MM-dd"
         */
        @JvmStatic
        fun setDateFormat(format: String) {
            dateFormat = SimpleDateFormat(format)
        }

        @JvmStatic
        fun setDateFormat(format: SimpleDateFormat) {
            dateFormat = format
        }
    }

    override fun filterByParam(query: Q, value: String) {
        if (value.contains("|")) {
            // Range format: start|end
            val parts = value.split("|")
            if (parts.size >= 2) {
                val start = parts[0].trim()
                val end = parts[1].trim()
                
                if (start.isNotEmpty()) {
                    query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, parseDate(start))
                }
                if (end.isNotEmpty()) {
                    query.where(internalName, BaradumOperator.LESS_OR_EQUAL, parseDate(end))
                }
            }
        } else {
            // Check for comparison operators (>, >=, <, <=, <>)
            val operator = getOperator(value)
            val cleanedValue = cleanValue(value)
            
            query.where(internalName, operator, parseDate(cleanedValue))
        }
    }

    protected open fun parseDate(value: String): Any {
        return try {
            // Try java.time formats first
            if (value.contains("T")) {
                LocalDateTime.parse(value, dateTimeFormatter)
            } else if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                LocalDate.parse(value, dateFormatter)
            } else {
                // Fall back to java.util.Date
                dateFormat.parse(value)
            }
        } catch (e: Exception) {
            try {
                // Try util.Date as fallback
                dateFormat.parse(value)
            } catch (e2: Exception) {
                throw FilterException("Invalid date format for '$value'. Expected: yyyy-MM-dd or ${dateFormat.toPattern()}")
            }
        }
    }

    override fun transform(value: String): Any {
        return parseDate(value)
    }
}
