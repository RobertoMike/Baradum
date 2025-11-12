package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Optional

/**
 * Comprehensive test suite for DateFilter.
 * Tests all date types, patterns, operators, range queries, and error conditions.
 */
class DateFilterTest {

    // Simple test query builder to capture calls
    class TestQueryBuilder : QueryBuilder<TestQueryBuilder> {
        data class WhereCall(val field: String, val operator: BaradumOperator, val value: Any?)
        
        val whereCalls = mutableListOf<WhereCall>()
        
        override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): TestQueryBuilder {
            whereCalls.add(WhereCall(field, operator, value))
            return this
        }
        
        override fun where(field: String, value: Any?): TestQueryBuilder {
            whereCalls.add(WhereCall(field, BaradumOperator.EQUAL, value))
            return this
        }
        
        override fun orderBy(field: String, direction: SortDirection): TestQueryBuilder = this
        override fun select(vararg fields: String): TestQueryBuilder = this
        override fun addSelect(vararg fields: String): TestQueryBuilder = this
        override fun limit(limit: Int): TestQueryBuilder = this
        override fun offset(offset: Long): TestQueryBuilder = this
        override fun get(): List<TestQueryBuilder> = emptyList()
        override fun page(limit: Int, offset: Long): Page<TestQueryBuilder> {
            return Page(emptyList(), 0, 0, 0)
        }
        override fun findFirst(): Optional<TestQueryBuilder> = Optional.empty()
        override fun getWhereConditions(): Any? = null
    }

    // ============================================
    // Constructor and Factory Method Tests
    // ============================================

    @Test
    fun `DateFilter default constructor uses LocalDate type`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertEquals(1, query.whereCalls.size)
        assertEquals("createdAt", query.whereCalls[0].field)
        assertTrue(query.whereCalls[0].value is LocalDate)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `DateFilter with custom internal name`() {
        val filter = DateFilter("date", "created_at")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertEquals("created_at", query.whereCalls[0].field)
    }

    @Test
    fun `forLocalDate factory creates LocalDate filter`() {
        val filter = DateFilter.forLocalDate("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is LocalDate)
    }

    @Test
    fun `forLocalDateTime factory creates LocalDateTime filter`() {
        val filter = DateFilter.forLocalDateTime("updatedAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15T10:30:00")

        assertTrue(query.whereCalls[0].value is LocalDateTime)
        assertEquals(LocalDateTime.parse("2024-01-15T10:30:00"), query.whereCalls[0].value)
    }

    @Test
    fun `forUtilDate factory creates java util Date filter`() {
        val filter = DateFilter.forUtilDate("birthDate")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is Date)
    }

    @Test
    fun `forSqlDate factory creates java sql Date filter`() {
        val filter = DateFilter.forSqlDate("startDate")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is java.sql.Date)
    }

    @Test
    fun `forSqlTimestamp factory creates java sql Timestamp filter`() {
        val filter = DateFilter.forSqlTimestamp("eventTime")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is java.sql.Timestamp)
    }

    // ============================================
    // Builder Pattern Tests
    // ============================================

    @Test
    fun `builder creates default LocalDate filter`() {
        val filter = DateFilter.builder("createdAt").build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is LocalDate)
    }

    @Test
    fun `builder with custom internal name`() {
        val filter = DateFilter.builder("date")
            .withInternalName("created_at")
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertEquals("created_at", query.whereCalls[0].field)
    }

    @Test
    fun `builder useLocalDate method`() {
        val filter = DateFilter.builder("date")
            .useLocalDate()
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is LocalDate)
    }

    @Test
    fun `builder useLocalDateTime method`() {
        val filter = DateFilter.builder("date")
            .useLocalDateTime()
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15T10:30:00")

        assertTrue(query.whereCalls[0].value is LocalDateTime)
    }

    @Test
    fun `builder useUtilDate method`() {
        val filter = DateFilter.builder("date")
            .useUtilDate()
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is Date)
    }

    @Test
    fun `builder useSqlDate method`() {
        val filter = DateFilter.builder("date")
            .useSqlDate()
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is java.sql.Date)
    }

    @Test
    fun `builder useSqlTimestamp method`() {
        val filter = DateFilter.builder("date")
            .useSqlTimestamp()
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertTrue(query.whereCalls[0].value is java.sql.Timestamp)
    }

    @Test
    fun `builder withPattern method for LocalDate`() {
        val filter = DateFilter.builder("date")
            .useLocalDate()
            .withPattern("dd/MM/yyyy")
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "15/01/2024")

        assertEquals(LocalDate.of(2024, 1, 15), query.whereCalls[0].value)
    }

    @Test
    fun `builder withPattern method for LocalDateTime`() {
        val filter = DateFilter.builder("date")
            .useLocalDateTime()
            .withPattern("dd/MM/yyyy HH:mm:ss")
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "15/01/2024 10:30:45")

        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 45), query.whereCalls[0].value)
    }

    @Test
    fun `builder withPattern method for UtilDate`() {
        val filter = DateFilter.builder("date")
            .useUtilDate()
            .withPattern("MM-dd-yyyy")
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "01-15-2024")

        assertTrue(query.whereCalls[0].value is Date)
    }

    @Test
    fun `builder chain all methods`() {
        val filter = DateFilter.builder("searchDate")
            .withInternalName("event_date")
            .useLocalDateTime()
            .withPattern("yyyy/MM/dd HH:mm")
            .build()
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024/01/15 10:30")

        assertEquals("event_date", query.whereCalls[0].field)
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), query.whereCalls[0].value)
    }

    // ============================================
    // Custom Pattern Tests
    // ============================================

    @Test
    fun `forLocalDate with custom pattern`() {
        val filter = DateFilter.forLocalDate("date", "dd-MM-yyyy")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "15-01-2024")

        assertEquals(LocalDate.of(2024, 1, 15), query.whereCalls[0].value)
    }

    @Test
    fun `forLocalDateTime with custom pattern`() {
        val filter = DateFilter.forLocalDateTime("timestamp", "yyyy/MM/dd HH:mm:ss")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024/01/15 14:30:45")

        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 45), query.whereCalls[0].value)
    }

    @Test
    fun `forUtilDate with custom pattern`() {
        val filter = DateFilter.forUtilDate("date", "MM-dd-yyyy")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "01-15-2024")

        assertTrue(query.whereCalls[0].value is Date)
    }

    // ============================================
    // Operator Tests (>, <, >=, <=, <>)
    // ============================================

    @Test
    fun `filter with greater than operator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, ">2024-01-15")

        assertEquals(BaradumOperator.GREATER, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with greater or equal operator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, ">=2024-01-15")

        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with less than operator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "<2024-01-15")

        assertEquals(BaradumOperator.LESS, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with less or equal operator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "<=2024-01-15")

        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with not equal operator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "<>2024-01-15")

        assertEquals(BaradumOperator.DIFF, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    @Test
    fun `filter without operator defaults to EQUAL`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertEquals(BaradumOperator.EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-15"), query.whereCalls[0].value)
    }

    // ============================================
    // Range Query Tests (pipe separator)
    // ============================================

    @Test
    fun `filter with range using pipe separator`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-01|2024-12-31")

        assertEquals(2, query.whereCalls.size)
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-01"), query.whereCalls[0].value)
        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[1].operator)
        assertEquals(LocalDate.parse("2024-12-31"), query.whereCalls[1].value)
    }

    @Test
    fun `filter with range start date only`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-01|")

        assertEquals(1, query.whereCalls.size)
        assertEquals(BaradumOperator.GREATER_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-01-01"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with range end date only`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "|2024-12-31")

        assertEquals(1, query.whereCalls.size)
        assertEquals(BaradumOperator.LESS_OR_EQUAL, query.whereCalls[0].operator)
        assertEquals(LocalDate.parse("2024-12-31"), query.whereCalls[0].value)
    }

    @Test
    fun `filter with range and whitespace`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "  2024-01-01  |  2024-12-31  ")

        assertEquals(2, query.whereCalls.size)
        assertEquals(LocalDate.parse("2024-01-01"), query.whereCalls[0].value)
        assertEquals(LocalDate.parse("2024-12-31"), query.whereCalls[1].value)
    }

    @Test
    fun `filter with range both dates empty does not create where clauses`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "|")

        assertEquals(0, query.whereCalls.size)
    }

    @Test
    fun `filter with LocalDateTime range`() {
        val filter = DateFilter.forLocalDateTime("updatedAt")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-01T00:00:00|2024-12-31T23:59:59")

        assertEquals(2, query.whereCalls.size)
        assertEquals(LocalDateTime.parse("2024-01-01T00:00:00"), query.whereCalls[0].value)
        assertEquals(LocalDateTime.parse("2024-12-31T23:59:59"), query.whereCalls[1].value)
    }

    // ============================================
    // Transform Method Tests
    // ============================================

    @Test
    fun `transform method returns LocalDate`() {
        val filter = DateFilter("date")
        
        val result = filter.transform("2024-01-15")
        
        assertTrue(result is LocalDate)
        assertEquals(LocalDate.parse("2024-01-15"), result)
    }

    @Test
    fun `transform method returns LocalDateTime`() {
        val filter = DateFilter.forLocalDateTime("date")
        
        val result = filter.transform("2024-01-15T10:30:00")
        
        assertTrue(result is LocalDateTime)
        assertEquals(LocalDateTime.parse("2024-01-15T10:30:00"), result)
    }

    @Test
    fun `transform method with custom pattern`() {
        val filter = DateFilter.forLocalDate("date", "dd/MM/yyyy")
        
        val result = filter.transform("15/01/2024")
        
        assertEquals(LocalDate.of(2024, 1, 15), result)
    }

    // ============================================
    // Error Handling Tests
    // ============================================

    @Test
    fun `invalid date format throws FilterException`() {
        val filter = DateFilter("createdAt")
        val query = TestQueryBuilder()

        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "invalid-date")
        }

        assertTrue(exception.message!!.contains("Invalid date format"))
        assertTrue(exception.message!!.contains("createdAt"))
    }

    @Test
    fun `invalid LocalDateTime format throws FilterException`() {
        val filter = DateFilter.forLocalDateTime("updatedAt")
        val query = TestQueryBuilder()

        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "2024-01-15")
        }

        assertTrue(exception.message!!.contains("Invalid date format"))
    }

    @Test
    fun `invalid custom pattern throws FilterException`() {
        val filter = DateFilter.forLocalDate("date", "dd/MM/yyyy")
        val query = TestQueryBuilder()

        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "2024-01-15")
        }

        assertTrue(exception.message!!.contains("Invalid date format"))
        assertTrue(exception.message!!.contains("dd/MM/yyyy"))
    }

    @Test
    fun `transform with invalid date throws FilterException`() {
        val filter = DateFilter("date")

        val exception = assertThrows<FilterException> {
            filter.transform("not-a-date")
        }

        assertTrue(exception.message!!.contains("Invalid date format"))
    }

    @Test
    fun `FilterException includes expected pattern in message`() {
        val filter = DateFilter.forLocalDate("date", "yyyy/MM/dd")
        val query = TestQueryBuilder()

        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "2024-13-45")
        }

        assertTrue(exception.message!!.contains("yyyy/MM/dd"))
    }

    @Test
    fun `FilterException includes date type in message`() {
        val filter = DateFilter.forLocalDateTime("timestamp")
        val query = TestQueryBuilder()

        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "invalid")
        }

        assertTrue(exception.message!!.contains("LOCAL_DATE_TIME"))
    }

    // ============================================
    // Edge Case Tests
    // ============================================

    @Test
    fun `filter with multiple pipe separators uses first two parts`() {
        val filter = DateFilter("date")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-01|2024-06-30|2024-12-31")

        assertEquals(2, query.whereCalls.size)
        assertEquals(LocalDate.parse("2024-01-01"), query.whereCalls[0].value)
        assertEquals(LocalDate.parse("2024-06-30"), query.whereCalls[1].value)
    }

    @Test
    fun `filter with operator and whitespace fails with proper error message`() {
        val filter = DateFilter("date")
        val query = TestQueryBuilder()

        // The cleanValue doesn't trim, so "  2024-01-15  " with spaces will fail parsing
        val exception = assertThrows<FilterException> {
            filter.filterByParam(query, "  >=  2024-01-15  ")
        }

        assertTrue(exception.message!!.contains("Invalid date format"))
    }

    @Test
    fun `factory methods with custom internal name`() {
        val filter = DateFilter.forLocalDate("search", null, "created_at")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        assertEquals("search", filter.param)
        assertEquals("created_at", query.whereCalls[0].field)
    }

    @Test
    fun `SqlDate converts from UtilDate correctly`() {
        val filter = DateFilter.forSqlDate("date")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        val result = query.whereCalls[0].value as java.sql.Date
        assertTrue(result is java.sql.Date)
    }

    @Test
    fun `SqlTimestamp converts from UtilDate correctly`() {
        val filter = DateFilter.forSqlTimestamp("timestamp")
        val query = TestQueryBuilder()

        filter.filterByParam(query, "2024-01-15")

        val result = query.whereCalls[0].value as java.sql.Timestamp
        assertTrue(result is java.sql.Timestamp)
    }
}
