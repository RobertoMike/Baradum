package io.github.robertomike.baradum.querydsl.performance

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.querydsl.BaseJpaTest
import io.github.robertomike.baradum.querydsl.QueryDslQueryBuilder
import io.github.robertomike.baradum.querydsl.entities.QUser
import io.github.robertomike.baradum.querydsl.entities.User
import io.github.robertomike.baradum.querydsl.entities.UserStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * Performance tests for the global path cache implementation.
 * 
 * These tests measure the impact of the global cache on query performance,
 * particularly focusing on reflection overhead reduction.
 */
class PathCachePerformanceTest : BaseJpaTest() {

    @BeforeEach
    fun setupTestData() {
        // Clear any existing data
        entityManager.createQuery("DELETE FROM User").executeUpdate()
        commit()
        entityManager.transaction.begin()
        
        // Create minimal test data (performance tests don't need much)
        val users = listOf(
            User(name = "Test User 1", email = "test1@example.com", age = 25, 
                 isActive = true, country = "USA", status = UserStatus.ACTIVE,
                 birthDate = LocalDate.of(1998, 5, 15), salary = 50000.0),
            
            User(name = "Test User 2", email = "test2@example.com", age = 30, 
                 isActive = true, country = "Canada", status = UserStatus.ACTIVE,
                 birthDate = LocalDate.of(1993, 8, 20), salary = 60000.0)
        )
        
        users.forEach { entityManager.persist(it) }
        commit()
        entityManager.transaction.begin()
    }
    
    @AfterEach
    fun cleanup() {
        rollback()
    }

    @Test
    fun `test cache performance - first vs subsequent queries`() {
        println("\n=== Cache Performance Test: First vs Subsequent Queries ===")
        
        // First query - will populate cache
        val firstQueryTime = measureNanoTime {
            val builder1 = QueryDslQueryBuilder(QUser.user, entityManager)
            builder1
                .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                .where("isActive", BaradumOperator.EQUAL, true)
                .orderBy("age", SortDirection.ASC)
                .orderBy("name", SortDirection.ASC)
                .get()
        }
        
        // Second query - should hit cache
        val secondQueryTime = measureNanoTime {
            val builder2 = QueryDslQueryBuilder(QUser.user, entityManager)
            builder2
                .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                .where("isActive", BaradumOperator.EQUAL, true)
                .orderBy("age", SortDirection.ASC)
                .orderBy("name", SortDirection.ASC)
                .get()
        }
        
        // Third query - should also hit cache
        val thirdQueryTime = measureNanoTime {
            val builder3 = QueryDslQueryBuilder(QUser.user, entityManager)
            builder3
                .where("age", BaradumOperator.LESS_OR_EQUAL, 65)
                .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                .orderBy("name", SortDirection.DESC)
                .get()
        }
        
        println("First query (cache population):  ${firstQueryTime / 1_000} μs")
        println("Second query (cache hit):        ${secondQueryTime / 1_000} μs")
        println("Third query (cache hit):         ${thirdQueryTime / 1_000} μs")
        
        // Cache should make subsequent queries faster (or at least not slower)
        // Note: This is just for visibility, actual speedup may vary based on JVM warmup
        val speedup = (firstQueryTime.toDouble() / secondQueryTime.toDouble())
        println("Speedup factor (first/second):   ${String.format("%.2f", speedup)}x")
        
        // Just verify queries work correctly
        assertTrue(secondQueryTime > 0, "Second query should execute")
        assertTrue(thirdQueryTime > 0, "Third query should execute")
    }

    @Test
    fun `test cache performance - multiple query instances`() {
        println("\n=== Cache Performance Test: Multiple Query Instances ===")
        
        val iterations = 100
        val times = mutableListOf<Long>()
        
        // Run many queries to see cache effect
        repeat(iterations) { i ->
            val time = measureNanoTime {
                val builder = QueryDslQueryBuilder(QUser.user, entityManager)
                builder
                    .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                    .where("email", BaradumOperator.LIKE, "%@example.com")
                    .where("isActive", BaradumOperator.EQUAL, true)
                    .orderBy("age", SortDirection.ASC)
                    .get()
            }
            times.add(time)
        }
        
        val first10Avg = times.take(10).average()
        val last10Avg = times.takeLast(10).average()
        val overallAvg = times.average()
        
        println("Iterations: $iterations")
        println("First 10 queries average:  ${first10Avg / 1_000} μs")
        println("Last 10 queries average:   ${last10Avg / 1_000} μs")
        println("Overall average:           ${overallAvg / 1_000} μs")
        println("Improvement:               ${String.format("%.2f", (first10Avg / last10Avg))}x faster")
        
        // After warmup, queries should be consistent or faster
        assertTrue(last10Avg > 0, "Queries should execute successfully")
    }

    @Test
    fun `test cache correctness - different fields same entity`() {
        println("\n=== Cache Correctness Test: Different Fields ===")
        
        // Query with different field combinations
        val builder1 = QueryDslQueryBuilder(QUser.user, entityManager)
        val results1 = builder1
            .where("age", BaradumOperator.GREATER_OR_EQUAL, 25)
            .where("country", BaradumOperator.EQUAL, "USA")
            .get()
        
        val builder2 = QueryDslQueryBuilder(QUser.user, entityManager)
        val results2 = builder2
            .where("email", BaradumOperator.LIKE, "%@example.com")
            .where("isActive", BaradumOperator.EQUAL, true)
            .get()
        
        val builder3 = QueryDslQueryBuilder(QUser.user, entityManager)
        val results3 = builder3
            .where("salary", BaradumOperator.GREATER_OR_EQUAL, 50000.0)
            .where("birthDate", BaradumOperator.IS_NOT_NULL, null)
            .get()
        
        println("Query 1 (age, country):      ${results1.size} results")
        println("Query 2 (email, isActive):   ${results2.size} results")
        println("Query 3 (salary, birthDate): ${results3.size} results")
        
        // Verify queries return correct results
        assertEquals(1, results1.size, "Should find 1 user >= 25 in USA")
        assertEquals(2, results2.size, "Should find 2 users with example.com emails")
        assertEquals(2, results3.size, "Should find 2 users with salary >= 50000")
    }

    @Test
    fun `test cache under heavy load - concurrent-like scenario`() {
        println("\n=== Heavy Load Test: Many Queries with Various Combinations ===")
        
        val totalQueries = 500
        val fieldCombinations = listOf(
            listOf("age", "status", "isActive"),
            listOf("email", "country", "name"),
            listOf("salary", "birthDate", "age"),
            listOf("status", "isActive", "country"),
            listOf("name", "email", "age")
        )
        
        val startTime = System.currentTimeMillis()
        var successfulQueries = 0
        
        repeat(totalQueries) { i ->
            val fields = fieldCombinations[i % fieldCombinations.size]
            
            try {
                val builder = QueryDslQueryBuilder(QUser.user, entityManager)
                
                // Apply filters for each field combination
                when (fields[0]) {
                    "age" -> builder.where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                    "email" -> builder.where("email", BaradumOperator.LIKE, "%@example.com")
                    "salary" -> builder.where("salary", BaradumOperator.GREATER_OR_EQUAL, 40000.0)
                    "status" -> builder.where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                    "name" -> builder.where("name", BaradumOperator.LIKE, "%User%")
                }
                
                when (fields[1]) {
                    "status" -> builder.where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                    "country" -> builder.where("country", BaradumOperator.IN, listOf("USA", "Canada"))
                    "birthDate" -> builder.where("birthDate", BaradumOperator.IS_NOT_NULL, null)
                    "isActive" -> builder.where("isActive", BaradumOperator.EQUAL, true)
                    "email" -> builder.where("email", BaradumOperator.LIKE, "%@%")
                }
                
                builder.orderBy(fields[2], if (i % 2 == 0) SortDirection.ASC else SortDirection.DESC)
                builder.get()
                
                successfulQueries++
            } catch (e: Exception) {
                println("Query $i failed: ${e.message}")
            }
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        val avgTimePerQuery = totalTime.toDouble() / totalQueries
        
        println("Total queries executed:    $totalQueries")
        println("Successful queries:        $successfulQueries")
        println("Total time:                ${totalTime}ms")
        println("Average time per query:    ${String.format("%.2f", avgTimePerQuery)}ms")
        println("Queries per second:        ${String.format("%.0f", 1000.0 / avgTimePerQuery)}")
        
        // Verify all queries succeeded
        assertEquals(totalQueries, successfulQueries, "All queries should succeed")
        
        // Performance assertion - should complete in reasonable time
        assertTrue(totalTime < 30000, "Should complete $totalQueries queries in less than 30 seconds")
    }

    @Test
    fun `test cache memory efficiency - unique fields cached`() {
        println("\n=== Cache Memory Efficiency Test ===")
        
        // Use many different field combinations
        val builder1 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder1.where("age", BaradumOperator.EQUAL, 25).get()
        
        val builder2 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder2.where("name", BaradumOperator.LIKE, "%Test%").get()
        
        val builder3 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder3.where("email", BaradumOperator.EQUAL, "test@example.com").get()
        
        val builder4 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder4.where("salary", BaradumOperator.GREATER, 40000.0).get()
        
        val builder5 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder5.where("birthDate", BaradumOperator.IS_NOT_NULL, null).get()
        
        val builder6 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder6.where("country", BaradumOperator.IN, listOf("USA")).get()
        
        val builder7 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder7.where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE).get()
        
        val builder8 = QueryDslQueryBuilder(QUser.user, entityManager)
        builder8.where("isActive", BaradumOperator.EQUAL, true).get()
        
        println("Executed 8 builders with 8 unique fields")
        println("Cache should contain entries for User entity with fields:")
        println("  - age, name, email, salary, birthDate, country, status, isActive")
        
        // Reuse same fields - should hit cache
        val reuseTime = measureNanoTime {
            val builder9 = QueryDslQueryBuilder(QUser.user, entityManager)
            builder9
                .where("age", BaradumOperator.EQUAL, 30)
                .where("name", BaradumOperator.LIKE, "%2%")
                .where("email", BaradumOperator.LIKE, "%@%")
                .get()
        }
        
        println("Reuse query time: ${reuseTime / 1_000} μs (should be fast with cache hits)")
        
        assertTrue(reuseTime > 0, "Reuse query should execute")
    }

    @Test
    fun `test cache with complex queries - multiple operators per field`() {
        println("\n=== Complex Query Test: Multiple Operators ===")
        
        val complexQueryTime = measureTimeMillis {
            repeat(50) {
                val builder = QueryDslQueryBuilder(QUser.user, entityManager)
                builder
                    // Multiple conditions on age
                    .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                    .where("age", BaradumOperator.LESS_OR_EQUAL, 65)
                    // Multiple conditions on email
                    .where("email", BaradumOperator.LIKE, "%@example.com")
                    .where("email", BaradumOperator.NOT_LIKE, "%spam%")
                    // Other fields
                    .where("isActive", BaradumOperator.EQUAL, true)
                    .where("status", BaradumOperator.IN, listOf(UserStatus.ACTIVE, UserStatus.PENDING))
                    .where("salary", BaradumOperator.BETWEEN, 40000.0 to 100000.0)
                    // Sorting
                    .orderBy("age", SortDirection.ASC)
                    .orderBy("name", SortDirection.ASC)
                    .get()
            }
        }
        
        println("50 complex queries (7 filters + 2 sorts each): ${complexQueryTime}ms")
        println("Average per query: ${complexQueryTime / 50.0}ms")
        
        // Should complete reasonably fast with cache
        assertTrue(complexQueryTime < 5000, "50 complex queries should complete in less than 5 seconds")
    }

    @Test
    fun `test cache behavior - field reuse patterns`() {
        println("\n=== Field Reuse Pattern Test ===")
        
        // Pattern 1: Same field multiple times in same query (cache hit within query)
        val pattern1Time = measureNanoTime {
            val builder = QueryDslQueryBuilder(QUser.user, entityManager)
            builder
                .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
                .where("age", BaradumOperator.LESS_OR_EQUAL, 65)
                .where("age", BaradumOperator.NOT_IN, listOf(30, 40, 50))
                .get()
        }
        
        // Pattern 2: Different fields (varied cache access)
        val pattern2Time = measureNanoTime {
            val builder = QueryDslQueryBuilder(QUser.user, entityManager)
            builder
                .where("age", BaradumOperator.EQUAL, 25)
                .where("name", BaradumOperator.LIKE, "%Test%")
                .where("email", BaradumOperator.LIKE, "%@example.com")
                .get()
        }
        
        // Pattern 3: Repeated common fields (typical real-world pattern)
        val pattern3Time = measureNanoTime {
            repeat(10) {
                val builder = QueryDslQueryBuilder(QUser.user, entityManager)
                builder
                    .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
                    .where("isActive", BaradumOperator.EQUAL, true)
                    .get()
            }
        }
        
        println("Pattern 1 (same field 3x):      ${pattern1Time / 1_000} μs")
        println("Pattern 2 (3 different fields): ${pattern2Time / 1_000} μs")
        println("Pattern 3 (10 queries, 2 fields each): ${pattern3Time / 1_000} μs")
        println("Pattern 3 average per query:    ${pattern3Time / 10_000} μs")
        
        // All patterns should execute successfully
        assertTrue(pattern1Time > 0 && pattern2Time > 0 && pattern3Time > 0)
    }
}
