package io.github.robertomike.baradum.querydsl.extensions

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.filters.*
import io.github.robertomike.baradum.querydsl.BaseJpaTest
import io.github.robertomike.baradum.querydsl.entities.QUser
import io.github.robertomike.baradum.querydsl.entities.User
import io.github.robertomike.baradum.querydsl.entities.UserStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Tests for EntityPathBase extension functions that add Baradum functionality.
 * These tests verify that the extension methods work correctly and provide
 * convenient access to Baradum features directly from Q-classes.
 */
class EntityPathExtensionsTest : BaseJpaTest() {

    @BeforeEach
    fun setupTestData() {
        // Clear any existing data
        entityManager.createQuery("DELETE FROM User").executeUpdate()
        commit()
        entityManager.transaction.begin()
        
        // Create test data
        val users = listOf(
            User(name = "Alice Smith", email = "alice@example.com", age = 25, 
                 isActive = true, country = "USA", status = UserStatus.ACTIVE,
                 birthDate = LocalDate.of(1998, 5, 15), salary = 50000.0),
            
            User(name = "Bob Johnson", email = "bob@example.com", age = 30, 
                 isActive = true, country = "Canada", status = UserStatus.ACTIVE,
                 birthDate = LocalDate.of(1993, 8, 20), salary = 60000.0),
            
            User(name = "Charlie Brown", email = "charlie@example.com", age = 35, 
                 isActive = false, country = "USA", status = UserStatus.INACTIVE,
                 birthDate = LocalDate.of(1988, 3, 10), salary = 70000.0),
            
            User(name = "Diana Prince", email = "diana@example.com", age = 28, 
                 isActive = true, country = "UK", status = UserStatus.PENDING,
                 birthDate = LocalDate.of(1995, 12, 25), salary = 55000.0)
        )
        
        users.forEach { entityManager.persist(it) }
        commit()
        entityManager.transaction.begin()
    }
    
    @AfterEach
    fun cleanup() {
        rollback()
    }

    // ========== BARADUM() EXTENSION TESTS ==========
    
    @Test
    fun `test baradum() extension creates Baradum instance`() {
        // Use extension function to create Baradum instance
        val baradum = QUser.user.baradum(entityManager)
        
        assertNotNull(baradum)
        // Verify we can get all users
        val results = baradum.get()
        assertEquals(4, results.size)
    }
    
    @Test
    fun `test baradum() extension with filters list`() {
        val filters = listOf(
            GreaterFilter(User::age, "minAge", orEqual = true)
        )
        
        // Use extension function with filters list
        val baradum = QUser.user.baradum(entityManager, filters)
        
        // Use instance parameters instead of request
        val results = baradum
            .withParams(mapOf("minAge" to "30"))
            .get()
        
        assertEquals(2, results.size) // Bob (30) and Charlie (35)
        assertTrue(results.all { it.age >= 30 })
    }
    
    @Test
    fun `test baradum() extension with vararg filters`() {
        // Use extension function with vararg filters
        val baradum = QUser.user.baradum(
            entityManager,
            ExactFilter(User::country),  // Use country instead of status (string comparison)
            GreaterFilter(User::age, "minAge", orEqual = true),
            ExactFilter(User::isActive)  // Add isActive filter
        )
        
        val results = baradum
            .withParams(mapOf("country" to "USA", "minAge" to "25", "isActive" to "true"))
            .get()
        
        assertEquals(1, results.size) // Alice (USA, 25, active)
        assertTrue(results.all { it.country == "USA" && it.age >= 25 && it.isActive })
    }
    
    @Test
    fun `test baradum() extension with allowedFilters`() {
        val baradum = QUser.user
            .baradum(entityManager)
            .allowedFilters(
                ExactFilter(User::isActive),
                ExactFilter(User::country)
            )
        
        val results = baradum
            .withParams(mapOf("isActive" to "true", "country" to "USA"))
            .get()
        
        assertEquals(1, results.size) // Only Alice
        assertEquals("Alice Smith", results[0].name)
    }
    
    @Test
    fun `test baradum() extension with sorting`() {
        val baradum = QUser.user
            .baradum(entityManager)
            .allowedFilters(ExactFilter(User::isActive))
            .allowedSort("age")
        
        val results = baradum
            .withParams(mapOf("isActive" to "true", "sort" to "age"))
            .get()
        
        assertEquals(3, results.size)
        assertEquals(25, results[0].age) // Alice - sorted by age ascending
        assertEquals(28, results[1].age) // Diana
        assertEquals(30, results[2].age) // Bob
    }
    
    @Test
    fun `test baradum() extension with descending sort`() {
        val baradum = QUser.user
            .baradum(entityManager)
            .allowedSort("age")
        
        val results = baradum
            .withParams(mapOf("sort" to "-age"))
            .get()
        
        assertEquals(4, results.size)
        assertEquals(35, results[0].age) // Charlie - sorted by age descending
        assertEquals(30, results[1].age) // Bob
        assertEquals(28, results[2].age) // Diana
        assertEquals(25, results[3].age) // Alice
    }
    
    @Test
    fun `test baradum() extension with pagination`() {
        val baradum = QUser.user
            .baradum(entityManager)
            .allowedSort("age")
        
        val page = baradum
            .withParams(mapOf("sort" to "age", "limit" to "2", "offset" to "0"))
            .page(10, 0) // Default values, will be overridden by instance params
        
        assertEquals(4, page.totalElements)
        assertEquals(2, page.content.size)
        assertEquals(25, page.content[0].age) // Alice
        assertEquals(28, page.content[1].age) // Diana
    }

    // ========== QUERYBUILDER() EXTENSION TESTS ==========
    
    @Test
    fun `test queryBuilder() extension basic query`() {
        val results = QUser.user
            .queryBuilder(entityManager)
            .where("age", BaradumOperator.GREATER_OR_EQUAL, 30)
            .get()
        
        assertEquals(2, results.size)
        assertTrue(results.all { it.age >= 30 })
    }
    
    @Test
    fun `test queryBuilder() extension with ordering`() {
        val results = QUser.user
            .queryBuilder(entityManager)
            .where("isActive", BaradumOperator.EQUAL, true)
            .orderBy("age", SortDirection.DESC)
            .get()
        
        assertEquals(3, results.size)
        assertEquals(30, results[0].age) // Bob
        assertEquals(28, results[1].age) // Diana
        assertEquals(25, results[2].age) // Alice
    }
    
    @Test
    fun `test queryBuilder() extension with multiple conditions`() {
        val results = QUser.user
            .queryBuilder(entityManager)
            .where("age", BaradumOperator.GREATER_OR_EQUAL, 25)
            .where("age", BaradumOperator.LESS_OR_EQUAL, 30)
            .orderBy("age", SortDirection.ASC)
            .get()
        
        assertEquals(3, results.size)
        assertEquals("Alice Smith", results[0].name)
        assertEquals("Diana Prince", results[1].name)
        assertEquals("Bob Johnson", results[2].name)
    }
    
    @Test
    fun `test queryBuilder() extension with pagination`() {
        val page = QUser.user
            .queryBuilder(entityManager)
            .orderBy("age", SortDirection.ASC)
            .page(2, 0)
        
        assertEquals(4, page.totalElements)
        assertEquals(2, page.content.size)
        assertEquals(25, page.content[0].age)
        assertEquals(28, page.content[1].age)
    }
    
    @Test
    fun `test queryBuilder() extension findFirst()`() {
        val result = QUser.user
            .queryBuilder(entityManager)
            .where("email", BaradumOperator.EQUAL, "bob@example.com")
            .findFirst()
        
        assertTrue(result.isPresent)
        assertEquals("Bob Johnson", result.get().name)
    }
    
    @Test
    fun `test queryBuilder() extension findFirst() with no results`() {
        val result = QUser.user
            .queryBuilder(entityManager)
            .where("email", BaradumOperator.EQUAL, "nonexistent@example.com")
            .findFirst()
        
        assertFalse(result.isPresent)
    }

    // ========== FLUENT API TESTS ==========
    
    @Test
    fun `test extension functions provide fluent API`() {
        // This demonstrates the fluent, chainable API
        val results = QUser.user
            .baradum(entityManager)
            .allowedFilters(
                ExactFilter(User::country),
                ExactFilter(User::isActive)
            )
            .allowedSort("age", "name")
            .withParams(mapOf("country" to "USA", "isActive" to "true", "sort" to "-age"))
            .get()
        
        assertEquals(1, results.size) // Only Alice is active in USA
        assertEquals("Alice Smith", results[0].name)
    }
    
    @Test
    fun `test combining extension with builder access`() {
        val baradum = QUser.user.baradum(entityManager)
        
        // Access builder for custom operations
        baradum.builder { builder ->
            builder.where("isActive", BaradumOperator.EQUAL, true)
            builder.orderBy("age", SortDirection.ASC)
        }
        
        val results = baradum.get()
        
        assertEquals(3, results.size)
        assertTrue(results.all { it.isActive })
        assertEquals(25, results[0].age) // Sorted by age
    }
    
    @Test
    fun `test extensions work with type-safe Kotlin property references`() {
        // Extension methods preserve type safety with Kotlin property references
        val results = QUser.user
            .baradum(
                entityManager,
                ExactFilter(User::country),  // Type-safe property reference
                GreaterFilter(User::age, "minAge", orEqual = true),  // Type-safe
                ExactFilter(User::isActive)  // Add isActive filter
            )
            .allowedSort("age")
            .withParams(mapOf("country" to "USA", "minAge" to "25", "isActive" to "true", "sort" to "age"))
            .get()
        
        assertEquals(1, results.size)
        assertTrue(results.all { it.country == "USA" && it.age >= 25 && it.isActive })
    }

    @Test
    fun `QueryDslBaradum factory with filters using EntityManager`() {
        val filters = listOf(ExactFilter("country"), ExactFilter("isActive"))
        val baradum = io.github.robertomike.baradum.querydsl.QueryDslBaradum.make(QUser.user, entityManager, filters)
        
        val results = baradum
            .withParams(mapOf("country" to "USA", "isActive" to "true"))
            .get()
        
        assertEquals(1, results.size)
        assertTrue(results.all { it.country == "USA" && it.isActive })
    }
}
