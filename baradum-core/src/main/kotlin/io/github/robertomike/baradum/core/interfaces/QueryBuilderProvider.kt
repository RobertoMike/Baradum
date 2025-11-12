package io.github.robertomike.baradum.core.interfaces

/**
 * Service Provider Interface for QueryBuilder factories.
 * Implementations should be registered via Java's ServiceLoader mechanism.
 *
 * To register an implementation:
 * 1. Implement this interface in your module
 * 2. Create file: META-INF/services/io.github.robertomike.baradum.core.interfaces.QueryBuilderProvider
 * 3. Add the fully qualified class name of your implementation
 *
 * Example implementation:
 * ```kotlin
 * class HefestoQueryBuilderProvider : QueryBuilderProvider {
 *     override fun <T> create(modelClass: Class<T>): QueryBuilder<T> {
 *         return HefestoQueryBuilder(modelClass)
 *     }
 *
 *     override fun getName(): String = "hefesto"
 * }
 * ```
 */
interface QueryBuilderProvider {
    /**
     * Create a QueryBuilder instance for the given model class
     */
    fun <T> create(modelClass: Class<T>): QueryBuilder<T>

    /**
     * Get the name/identifier of this provider (e.g., "hefesto", "jooq", "querydsl")
     */
    fun getName(): String

    /**
     * Check if this provider supports the given model class
     * Default implementation returns true for all classes
     */
    fun supports(modelClass: Class<*>): Boolean = true
}