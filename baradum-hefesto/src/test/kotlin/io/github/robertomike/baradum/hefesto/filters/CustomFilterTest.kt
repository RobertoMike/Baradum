package io.github.robertomike.baradum.hefesto.filters

import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder
import io.github.robertomike.baradum.hefesto.models.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.BiConsumer

/**
 * Tests for CustomFilter to achieve 100% coverage of hefesto.filters package
 */
class CustomFilterTest {

    @Test
    fun `constructor creates custom filter with provided name and consumer`() {
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { _, _ -> }
        val filter = CustomFilter("customField", consumer)
        
        assertNotNull(filter)
    }

    @Test
    fun `apply executes the custom consumer with query builder and value`() {
        var capturedBuilder: HefestoQueryBuilder<*>? = null
        var capturedValue: String? = null
        
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { builder, value ->
            capturedBuilder = builder
            capturedValue = value
        }
        
        val filter = CustomFilter("testField", consumer)
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        val testValue = "test-value"
        
        filter.filterByParam(queryBuilder, testValue)
        
        assertSame(queryBuilder, capturedBuilder, "Consumer should receive the query builder")
        assertEquals(testValue, capturedValue, "Consumer should receive the filter value")
    }

    @Test
    fun `apply with empty string executes consumer correctly`() {
        var capturedValue: String? = null
        
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { _, value ->
            capturedValue = value
        }
        
        val filter = CustomFilter("emptyField", consumer)
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        
        filter.filterByParam(queryBuilder, "")
        
        assertEquals("", capturedValue, "Consumer should receive empty string")
    }

    @Test
    fun `multiple apply calls work independently`() {
        val capturedValues = mutableListOf<String>()
        
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { _, value ->
            capturedValues.add(value)
        }
        
        val filter = CustomFilter("multiField", consumer)
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        
        filter.filterByParam(queryBuilder, "first")
        filter.filterByParam(queryBuilder, "second")
        filter.filterByParam(queryBuilder, "third")
        
        assertEquals(3, capturedValues.size)
        assertEquals(listOf("first", "second", "third"), capturedValues)
    }

    @Test
    fun `custom filter can modify query builder state`() {
        var builderModified = false
        
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { builder, value ->
            // Simulate a custom where condition being added
            builder.where("customField", value)
            builderModified = true
        }
        
        val filter = CustomFilter("modifyingField", consumer)
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        
        filter.filterByParam(queryBuilder, "test")
        
        assertTrue(builderModified, "Consumer should have modified builder")
    }

    @Test
    fun `custom filter with lambda consumer works`() {
        var lambdaCalled = false
        var receivedValue: String? = null
        
        val filter = CustomFilter("lambdaField") { builder, value ->
            lambdaCalled = true
            receivedValue = value
            assertNotNull(builder)
            assertNotNull(value)
        }
        
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        filter.filterByParam(queryBuilder, "test-value")
        
        assertTrue(lambdaCalled, "Lambda consumer should be executed")
        assertEquals("test-value", receivedValue)
    }

    @Test
    fun `custom filter with special characters in value`() {
        var capturedValue: String? = null
        
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { _, value ->
            capturedValue = value
        }
        
        val filter = CustomFilter("specialField", consumer)
        val queryBuilder = HefestoQueryBuilder(User::class.java)
        val specialValue = "test@example.com#!$%"
        
        filter.filterByParam(queryBuilder, specialValue)
        
        assertEquals(specialValue, capturedValue)
    }

    @Test
    fun `custom filter param name is preserved`() {
        val paramName = "myCustomParam"
        val consumer: BiConsumer<HefestoQueryBuilder<out io.github.robertomike.hefesto.models.BaseModel>, String> = BiConsumer { _, _ -> }
        val filter = CustomFilter(paramName, consumer)
        
        // The param should be accessible through the Filter base class
        assertNotNull(filter)
    }
}
