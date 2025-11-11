package io.github.robertomike.baradum.querydsl

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.core.filters.Filter
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager

/**
 * Factory for creating Baradum instances with QueryDSL backend
 * 
 * This provides a convenient way to create Baradum instances configured
 * to use QueryDSL for query execution.
 * 
 * Usage example:
 * ```kotlin
 * val baradum = QueryDslBaradum.make(QUser.user, entityManager)
 * baradum.addFilter(ExactFilter("name"))
 * baradum.addFilter(GreaterFilter(User::age, orEqual = true))
 * val users = baradum.applyFilters(request).get()
 * ```
 */
object QueryDslBaradum {
    /**
     * Creates a new Baradum instance with QueryDSL query builder using EntityManager
     */
    @JvmStatic
    fun <T> make(entityPath: EntityPathBase<T>, entityManager: EntityManager): Baradum<T, QueryDslQueryBuilder<T>> {
        val queryBuilder = QueryDslQueryBuilder(entityPath, entityManager)
        return Baradum(queryBuilder)
    }

    /**
     * Creates a new Baradum instance with QueryDSL query builder using JPAQueryFactory
     */
    @JvmStatic
    fun <T> make(entityPath: EntityPathBase<T>, queryFactory: JPAQueryFactory): Baradum<T, QueryDslQueryBuilder<T>> {
        val queryBuilder = QueryDslQueryBuilder(entityPath, queryFactory)
        return Baradum(queryBuilder)
    }

    /**
     * Creates a new Baradum instance with filters using EntityManager
     */
    @JvmStatic
    fun <T> make(
        entityPath: EntityPathBase<T>,
        entityManager: EntityManager,
        filters: Collection<Filter<*, *>>
    ): Baradum<T, QueryDslQueryBuilder<T>> {
        val baradum = make(entityPath, entityManager)
        baradum.allowedFilters(filters.toList())
        return baradum
    }

    /**
     * Creates a new Baradum instance with filters using JPAQueryFactory
     */
    @JvmStatic
    fun <T> make(
        entityPath: EntityPathBase<T>,
        queryFactory: JPAQueryFactory,
        filters: Collection<Filter<*, *>>
    ): Baradum<T, QueryDslQueryBuilder<T>> {
        val baradum = make(entityPath, queryFactory)
        baradum.allowedFilters(filters.toList())
        return baradum
    }
}
