package io.github.robertomike.baradum.hefesto

import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.hefesto.models.BaseModel
import jakarta.persistence.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for HefestoQueryBuilder
 * These tests verify the basic structure and API of the query builder
 */
class HefestoQueryBuilderTest {

    // Simple test model implementing BaseModel
    @Entity
    @Table(name = "test_models")
    class TestModel : BaseModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
        
        @Column(name = "name")
        var name: String? = null
        
        @Column(name = "age")
        var age: Int? = null
        
        override fun getTable(): String = "test_models"
    }

    @Test
    fun `HefestoQueryBuilder should implement QueryBuilder interface`() {
        // This test verifies that the class structure is correct
        val interfaces = HefestoQueryBuilder::class.java.interfaces
        assertTrue(
            interfaces.any { it.simpleName == "QueryBuilder" },
            "HefestoQueryBuilder should implement QueryBuilder interface"
        )
    }

    @Test
    fun `getBuilder should return builder when type matches`() {
        val queryBuilder = HefestoQueryBuilder(TestModel::class.java)
        
        // Should successfully cast to HefestoQueryBuilder
        val result = queryBuilder.getBuilder(HefestoQueryBuilder::class.java)
        
        assertNotNull(result)
        assertSame(queryBuilder, result)
        assertTrue(result is HefestoQueryBuilder<*>)
    }

    @Test
    fun `getBuilder should throw ClassCastException when type does not match`() {
        val queryBuilder = HefestoQueryBuilder(TestModel::class.java)
        
        // Should throw ClassCastException when trying to cast to incompatible type
        val exception = assertThrows(ClassCastException::class.java) {
            queryBuilder.getBuilder(String::class.java)
        }
        
        assertTrue(exception.message!!.contains("QueryBuilder is of type"))
        assertTrue(exception.message!!.contains("but expected java.lang.String"))
    }

    @Test
    fun `getBuilder should work with interface types`() {
        val queryBuilder = HefestoQueryBuilder(TestModel::class.java)
        
        // Should work with QueryBuilder interface
        val result = queryBuilder.getBuilder(QueryBuilder::class.java)
        
        assertNotNull(result)
        assertSame(queryBuilder, result)
    }

    @Test
    fun `getBuilder should throw descriptive exception with actual and expected types`() {
        val queryBuilder = HefestoQueryBuilder(TestModel::class.java)
        
        // Try casting to an incompatible type
        val exception = assertThrows(ClassCastException::class.java) {
            queryBuilder.getBuilder(ArrayList::class.java)
        }
        
        // Verify the error message contains both the actual type and expected type
        assertNotNull(exception.message)
        assertTrue(
            exception.message!!.contains("HefestoQueryBuilder"),
            "Error message should contain actual type HefestoQueryBuilder"
        )
        assertTrue(
            exception.message!!.contains("java.util.ArrayList"),
            "Error message should contain expected type ArrayList"
        )
    }
}
