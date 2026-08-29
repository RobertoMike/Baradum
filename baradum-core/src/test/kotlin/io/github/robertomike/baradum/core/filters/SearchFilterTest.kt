package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SearchLikeStrategy
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

/**
 * Tests for SearchFilter. Previously had zero direct coverage - this is exactly the kind of gap
 * that let outdated docs claim (incorrectly) that SearchFilter accepted a KProperty1 constructor.
 */
class SearchFilterTest {

    private lateinit var mockQueryBuilder: QueryBuilder<Any>

    @BeforeEach
    fun setup() {
        mockQueryBuilder = mock()
    }

    @Test
    fun `filterByParam applies LIKE across every field with OR`() {
        val filter = SearchFilter("search", "name", "email", "phone")
        filter.filterByParam(mockQueryBuilder, "john")

        verify(mockQueryBuilder).where("name", BaradumOperator.LIKE, "%john%", WhereOperator.AND)
        verify(mockQueryBuilder).where("email", BaradumOperator.LIKE, "%john%", WhereOperator.OR)
        verify(mockQueryBuilder).where("phone", BaradumOperator.LIKE, "%john%", WhereOperator.OR)
        verify(mockQueryBuilder, times(3)).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `of factory method defaults param to search`() {
        val filter = SearchFilter.of("name", "email")
        assertEquals("search", filter.param)
    }

    @Test
    fun `default strategy is COMPLETE unlike PartialFilter's FINAL`() {
        val filter = SearchFilter("search", "name")
        filter.filterByParam(mockQueryBuilder, "john")

        verify(mockQueryBuilder).where("name", BaradumOperator.LIKE, "%john%", WhereOperator.AND)
    }

    @Test
    fun `setStrategy changes the wildcard placement`() {
        val filter = SearchFilter("search", "name").setStrategy(SearchLikeStrategy.FINAL)
        filter.filterByParam(mockQueryBuilder, "john")

        verify(mockQueryBuilder).where("name", BaradumOperator.LIKE, "john%", WhereOperator.AND)
    }

    @Test
    fun `setInternalNames overrides the fields to search`() {
        val filter = SearchFilter("search", "name").setInternalNames(listOf("full_name", "nickname"))
        filter.filterByParam(mockQueryBuilder, "john")

        verify(mockQueryBuilder).where("full_name", BaradumOperator.LIKE, "%john%", WhereOperator.AND)
        verify(mockQueryBuilder).where("nickname", BaradumOperator.LIKE, "%john%", WhereOperator.OR)
    }

    @Test
    fun `setIgnoreCase emits LIKE_IGNORE_CASE for every field`() {
        val filter = SearchFilter("search", "name", "email").setIgnoreCase(true)
        filter.filterByParam(mockQueryBuilder, "JOHN")

        verify(mockQueryBuilder).where("name", BaradumOperator.LIKE_IGNORE_CASE, "%JOHN%", WhereOperator.AND)
        verify(mockQueryBuilder).where("email", BaradumOperator.LIKE_IGNORE_CASE, "%JOHN%", WhereOperator.OR)
    }

    @Test
    fun `filterByParam with no fields does nothing`() {
        val filter = SearchFilter("search")
        filter.filterByParam(mockQueryBuilder, "john")

        verify(mockQueryBuilder, never()).where(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }
}
