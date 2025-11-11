package io.github.robertomike.baradum.querydsl

import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager

/**
 * Provider for creating QueryDSL query builders
 * 
 * This provider creates instances of QueryDslQueryBuilder for a given entity type.
 * It requires an EntityPath (Q-class) and either a JPAQueryFactory or EntityManager.
 * 
 * Note: Unlike the generic QueryBuilderProvider interface, this class maintains
 * the entity type for type-safe query building with QueryDSL.
 */
class QueryDslQueryBuilderProvider<T>(
    private val entityPath: EntityPathBase<T>,
    private val queryFactory: JPAQueryFactory
) {

    /**
     * Secondary constructor that creates JPAQueryFactory from EntityManager
     */
    constructor(entityPath: EntityPathBase<T>, entityManager: EntityManager) 
        : this(entityPath, JPAQueryFactory(entityManager))

    /**
     * Create a new QueryBuilder instance
     */
    fun createQueryBuilder(): QueryBuilder<T> {
        return QueryDslQueryBuilder(entityPath, queryFactory)
    }

    /**
     * Get the entity path used by this provider
     */
    fun getEntityPath(): EntityPathBase<T> {
        return entityPath
    }

    /**
     * Get the query factory used by this provider
     */
    fun getQueryFactory(): JPAQueryFactory {
        return queryFactory
    }
}
