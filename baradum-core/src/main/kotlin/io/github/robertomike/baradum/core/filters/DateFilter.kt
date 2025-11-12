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
 * 
 * Supports multiple formats:
 * - Range with pipe: "2024-01-01|2024-12-31" -> BETWEEN dates
 * - With operators: ">2024-01-01", "<=2024-12-31", etc.
 * - Single date: "2024-01-01" -> exact match
 * 
 * Configurable date types:
 * - LocalDate (default)
 * - LocalDateTime
 * - java.util.Date
 * - java.sql.Date
 * - java.sql.Timestamp
 * 
 * Usage examples:
 * ```kotlin
 * // Simple usage with LocalDate
 * DateFilter("createdAt")
 * 
 * // Custom pattern with LocalDateTime
 * DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")
 * 
 * // java.util.Date with custom format
 * DateFilter.forUtilDate("birthDate", "MM-dd-yyyy")
 * 
 * // Builder pattern
 * DateFilter.builder("eventDate")
 *     .useLocalDate()
 *     .withPattern("yyyy/MM/dd")
 *     .build()
 * ```
 */
open class DateFilter @JvmOverloads constructor(
    param: String,
    internalName: String = param,
    private val dateType: DateType = DateType.LOCAL_DATE,
    private val pattern: String? = null
) : Filter<Any, QueryBuilder<*>>(param, internalName) {

    /**
     * Supported date types for parsing and filtering
     */
    enum class DateType {
        LOCAL_DATE,
        LOCAL_DATE_TIME,
        UTIL_DATE,
        SQL_DATE,
        SQL_TIMESTAMP
    }

    /**
     * Builder for creating customized DateFilter instances
     */
    class Builder(
        private val param: String,
        private var internalName: String = param
    ) {
        private var dateType: DateType = DateType.LOCAL_DATE
        private var pattern: String? = null

        fun withInternalName(name: String) = apply { this.internalName = name }
        fun useLocalDate() = apply { this.dateType = DateType.LOCAL_DATE }
        fun useLocalDateTime() = apply { this.dateType = DateType.LOCAL_DATE_TIME }
        fun useUtilDate() = apply { this.dateType = DateType.UTIL_DATE }
        fun useSqlDate() = apply { this.dateType = DateType.SQL_DATE }
        fun useSqlTimestamp() = apply { this.dateType = DateType.SQL_TIMESTAMP }
        fun withPattern(pattern: String) = apply { this.pattern = pattern }
        
        fun build(): DateFilter = DateFilter(param, internalName, dateType, pattern)
    }

    companion object {
        /**
         * Create a builder for DateFilter configuration
         */
        @JvmStatic
        fun builder(param: String): Builder = Builder(param)

        /**
         * Create a DateFilter for LocalDate with optional custom pattern
         */
        @JvmStatic
        @JvmOverloads
        fun forLocalDate(
            param: String,
            pattern: String? = null,
            internalName: String = param
        ): DateFilter = DateFilter(param, internalName, DateType.LOCAL_DATE, pattern)

        /**
         * Create a DateFilter for LocalDateTime with optional custom pattern
         */
        @JvmStatic
        @JvmOverloads
        fun forLocalDateTime(
            param: String,
            pattern: String? = null,
            internalName: String = param
        ): DateFilter = DateFilter(param, internalName, DateType.LOCAL_DATE_TIME, pattern)

        /**
         * Create a DateFilter for java.util.Date with optional custom pattern
         */
        @JvmStatic
        @JvmOverloads
        fun forUtilDate(
            param: String,
            pattern: String? = null,
            internalName: String = param
        ): DateFilter = DateFilter(param, internalName, DateType.UTIL_DATE, pattern)

        /**
         * Create a DateFilter for java.sql.Date with optional custom pattern
         */
        @JvmStatic
        @JvmOverloads
        fun forSqlDate(
            param: String,
            pattern: String? = null,
            internalName: String = param
        ): DateFilter = DateFilter(param, internalName, DateType.SQL_DATE, pattern)

        /**
         * Create a DateFilter for java.sql.Timestamp with optional custom pattern
         */
        @JvmStatic
        @JvmOverloads
        fun forSqlTimestamp(
            param: String,
            pattern: String? = null,
            internalName: String = param
        ): DateFilter = DateFilter(param, internalName, DateType.SQL_TIMESTAMP, pattern)
    }

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
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

    /**
     * Parse date string according to the configured date type and pattern.
     * 
     * @param value The date string to parse
     * @return Parsed date object of the configured type
     * @throws FilterException if parsing fails
     */
    protected open fun parseDate(value: String): Any {
        return try {
            when (dateType) {
                DateType.LOCAL_DATE -> parseLocalDate(value)
                DateType.LOCAL_DATE_TIME -> parseLocalDateTime(value)
                DateType.UTIL_DATE -> parseUtilDate(value)
                DateType.SQL_DATE -> java.sql.Date(parseUtilDate(value).time)
                DateType.SQL_TIMESTAMP -> java.sql.Timestamp(parseUtilDate(value).time)
            }
        } catch (e: Exception) {
            val expectedPattern = pattern ?: getDefaultPattern()
            throw FilterException(
                "Invalid date format for '$value' in filter '$param'. " +
                "Expected pattern: $expectedPattern, Date type: $dateType"
            )
        }
    }

    private fun parseLocalDate(value: String): LocalDate {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) } 
            ?: DateTimeFormatter.ISO_LOCAL_DATE
        return LocalDate.parse(value, formatter)
    }

    private fun parseLocalDateTime(value: String): LocalDateTime {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) } 
            ?: DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return LocalDateTime.parse(value, formatter)
    }

    private fun parseUtilDate(value: String): Date {
        val format = SimpleDateFormat(pattern ?: "yyyy-MM-dd")
        return format.parse(value)
    }

    private fun getDefaultPattern(): String {
        return when (dateType) {
            DateType.LOCAL_DATE -> "yyyy-MM-dd (ISO)"
            DateType.LOCAL_DATE_TIME -> "yyyy-MM-dd'T'HH:mm:ss (ISO)"
            DateType.UTIL_DATE, DateType.SQL_DATE -> "yyyy-MM-dd"
            DateType.SQL_TIMESTAMP -> "yyyy-MM-dd HH:mm:ss"
        }
    }

    override fun transform(value: String): Any {
        return parseDate(value)
    }
}
