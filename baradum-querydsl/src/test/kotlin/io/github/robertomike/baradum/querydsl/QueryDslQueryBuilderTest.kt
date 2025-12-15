package io.github.robertomike.baradum.querydsl

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.filters.ExactFilter
import io.github.robertomike.baradum.querydsl.entities.QUser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for QueryDslQueryBuilder without requiring database
 * These tests verify the basic structure and API of the query builder
 */
class QueryDslQueryBuilderTest : BaseJpaTest() {

    @Test
    fun `QueryDslQueryBuilder should implement QueryBuilder interface`() {
        // This test verifies that the class structure is correct
        val interfaces = QueryDslQueryBuilder::class.java.interfaces
        assertTrue(
            interfaces.any { it.simpleName == "QueryBuilder" },
            "QueryDslQueryBuilder should implement QueryBuilder interface"
        )
    }

    @Test
    fun `QueryDslBaradum should be an object with factory methods`() {
        // Verify that QueryDslBaradum is an object (singleton)
        val kotlinClass = QueryDslBaradum::class
        assertTrue(
            kotlinClass.objectInstance != null,
            "QueryDslBaradum should be a Kotlin object"
        )
    }

    @Test
    fun `BaradumOperator enum should have all required operators`() {
        val operators = BaradumOperator.values()
        
        assertTrue(operators.contains(BaradumOperator.EQUAL))
        assertTrue(operators.contains(BaradumOperator.DIFF))
        assertTrue(operators.contains(BaradumOperator.GREATER))
        assertTrue(operators.contains(BaradumOperator.GREATER_OR_EQUAL))
        assertTrue(operators.contains(BaradumOperator.LESS))
        assertTrue(operators.contains(BaradumOperator.LESS_OR_EQUAL))
        assertTrue(operators.contains(BaradumOperator.LIKE))
        assertTrue(operators.contains(BaradumOperator.NOT_LIKE))
        assertTrue(operators.contains(BaradumOperator.IN))
        assertTrue(operators.contains(BaradumOperator.NOT_IN))
        assertTrue(operators.contains(BaradumOperator.IS_NULL))
        assertTrue(operators.contains(BaradumOperator.IS_NOT_NULL))
        assertTrue(operators.contains(BaradumOperator.BETWEEN))
    }

    @Test
    fun `SortDirection enum should have ASC and DESC`() {
        val directions = SortDirection.values()
        
        assertTrue(directions.contains(SortDirection.ASC))
        assertTrue(directions.contains(SortDirection.DESC))
        assertEquals(2, directions.size)
    }

    @Test
    fun `WhereOperator enum should have AND and OR`() {
        val operators = WhereOperator.values()
        
        assertTrue(operators.contains(WhereOperator.AND))
        assertTrue(operators.contains(WhereOperator.OR))
        assertEquals(2, operators.size)
    }

    @Test
    fun `getBuilder should return builder when type matches`() {
        val queryBuilder = QueryDslQueryBuilder(QUser.user, entityManager)
        
        // Should successfully cast to QueryDslQueryBuilder
        val result = queryBuilder.getBuilder(QueryDslQueryBuilder::class.java)
        
        assertNotNull(result)
        assertSame(queryBuilder, result)
        assertTrue(result is QueryDslQueryBuilder<*>)
    }

    @Test
    fun `getBuilder should throw ClassCastException when type does not match`() {
        val queryBuilder = QueryDslQueryBuilder(QUser.user, entityManager)
        
        // Should throw ClassCastException when trying to cast to incompatible type
        val exception = assertThrows(ClassCastException::class.java) {
            queryBuilder.getBuilder(String::class.java)
        }
        
        assertTrue(exception.message!!.contains("QueryBuilder is of type"))
        assertTrue(exception.message!!.contains("but expected java.lang.String"))
    }

    @Test
    fun `getBuilder should work with interface types`() {
        val queryBuilder = QueryDslQueryBuilder(QUser.user, entityManager)
        
        // Should work with QueryBuilder interface
        val result = queryBuilder.getBuilder(io.github.robertomike.baradum.core.interfaces.QueryBuilder::class.java)
        
        assertNotNull(result)
        assertSame(queryBuilder, result)
    }
}
