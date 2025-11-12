package io.github.robertomike.baradum.querydsl.extensions

import com.querydsl.core.types.dsl.EntityPathBase
import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.querydsl.QueryDslBaradum
import io.github.robertomike.baradum.querydsl.QueryDslQueryBuilder
import jakarta.persistence.EntityManager

/**
 * Extension functions for QueryDSL EntityPathBase to add Baradum functionality.
 * 
 * These extensions allow you to use Baradum filtering directly on Q-classes:
 * 
 * ```kotlin
 * val users = QUser.user
 *     .baradum(entityManager)
 *     .allowedFilters(
 *         ExactFilter(User::name),
 *         GreaterFilter(User::age, orEqual = true)
 *     )
 *     .applyFilters(request)
 *     .get()
 * ```
 */

/**
 * Create a Baradum instance from a QueryDSL entity path with EntityManager.
 * 
 * @param entityManager The JPA EntityManager to use for queries
 * @return A Baradum instance configured with QueryDSL
 * 
 * @example
 * ```kotlin
 * QUser.user.baradum(entityManager)
 *     .allowedFilters(ExactFilter(User::name))
 *     .applyFilters(request)
 *     .get()
 * ```
 */
fun <T> EntityPathBase<T>.baradum(entityManager: EntityManager): Baradum<T, QueryDslQueryBuilder<T>> {
    return QueryDslBaradum.make(this, entityManager)
}

/**
 * Create a Baradum instance from a QueryDSL entity path with EntityManager and pre-configured filters.
 * 
 * @param entityManager The JPA EntityManager to use for queries
 * @param filters List of filters to apply
 * @return A Baradum instance configured with QueryDSL and the provided filters
 * 
 * @example
 * ```kotlin
 * val filters = listOf(
 *     ExactFilter(User::name),
 *     GreaterFilter(User::age, orEqual = true)
 * )
 * 
 * QUser.user.baradum(entityManager, filters)
 *     .applyFilters(request)
 *     .get()
 * ```
 */
fun <T> EntityPathBase<T>.baradum(
    entityManager: EntityManager,
    filters: List<Filter<*, *>>
): Baradum<T, QueryDslQueryBuilder<T>> {
    return QueryDslBaradum.make(this, entityManager, filters)
}

/**
 * Create a Baradum instance with vararg filters for convenience.
 * 
 * @param entityManager The JPA EntityManager to use for queries
 * @param filters Vararg of filters to apply
 * @return A Baradum instance configured with QueryDSL and the provided filters
 * 
 * @example
 * ```kotlin
 * QUser.user.baradum(
 *     entityManager,
 *     ExactFilter(User::name),
 *     GreaterFilter(User::age, orEqual = true),
 *     PartialFilter(User::email)
 * ).applyFilters(request).get()
 * ```
 */
fun <T> EntityPathBase<T>.baradum(
    entityManager: EntityManager,
    vararg filters: Filter<*, *>
): Baradum<T, QueryDslQueryBuilder<T>> {
    return QueryDslBaradum.make(this, entityManager, filters.toList())
}

/**
 * Create a QueryDslQueryBuilder directly from a QueryDSL entity path.
 * Use this when you want direct access to the query builder without Baradum's filter DSL.
 * 
 * @param entityManager The JPA EntityManager to use for queries
 * @return A QueryDslQueryBuilder for building type-safe queries
 * 
 * @example
 * ```kotlin
 * val users = QUser.user
 *     .queryBuilder(entityManager)
 *     .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
 *     .orderBy("name", SortDirection.ASC)
 *     .get()
 * ```
 */
fun <T> EntityPathBase<T>.queryBuilder(entityManager: EntityManager): QueryDslQueryBuilder<T> {
    return QueryDslQueryBuilder(this, entityManager)
}
