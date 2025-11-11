package io.github.robertomike.baradum.querydsl

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.querydsl.converters.SortConverter
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Path
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.*
import com.querydsl.core.types.Ops
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * QueryDSL implementation of QueryBuilder for Baradum
 * 
 * This implementation bridges Baradum's filter system with QueryDSL's type-safe query API.
 * It supports all standard filtering operations including where clauses, sorting, pagination,
 * and field selection.
 * 
 * @param T The entity type being queried
 * @param entityPath The QueryDSL EntityPath (Q-class) for the entity
 * @param queryFactory The JPAQueryFactory used to create queries
 */
class QueryDslQueryBuilder<T>(
    private val entityPath: EntityPathBase<T>,
    private val queryFactory: JPAQueryFactory
) : QueryBuilder<T> {

    private val query: JPAQuery<T> = queryFactory.selectFrom(entityPath)
    private val predicates = mutableListOf<Predicate>()
    private var currentWhereOperator = WhereOperator.AND
    
    companion object {
        /**
         * Global cache for field paths to avoid repeated reflection lookups across all query instances.
         * 
         * Key: Pair of (Entity Class, Field Name)
         * Value: Path expression
         * 
         * This static cache significantly improves performance in server applications where
         * the same entities are queried repeatedly across multiple requests. The cache is
         * thread-safe using ConcurrentHashMap.
         */
        private val globalPathCache = ConcurrentHashMap<Pair<Class<*>, String>, Path<*>>()
    }

    /**
     * Secondary constructor that creates JPAQueryFactory from EntityManager
     */
    constructor(entityPath: EntityPathBase<T>, entityManager: EntityManager) 
        : this(entityPath, JPAQueryFactory(entityManager))

    override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): QueryBuilder<T> {
        currentWhereOperator = whereOperator
        
        val path = getPath(field)
        val predicate = createPredicate(path, operator, value)
        
        predicates.add(predicate)
        applyPredicates()
        
        return this
    }

    override fun orderBy(field: String, direction: SortDirection): QueryBuilder<T> {
        val path = getPath(field)
        val order = SortConverter.toQueryDsl(direction)
        
        val orderSpecifier: OrderSpecifier<*> = when (path) {
            is StringPath -> OrderSpecifier(order, path)
            is NumberPath<*> -> OrderSpecifier(order, path)
            is DatePath<*> -> OrderSpecifier(order, path)
            is DateTimePath<*> -> OrderSpecifier(order, path)
            is BooleanPath -> OrderSpecifier(order, path)
            is ComparablePath<*> -> OrderSpecifier(order, path)
            else -> OrderSpecifier(order, path as ComparableExpressionBase<*>)
        }
        
        query.orderBy(orderSpecifier)
        return this
    }

    override fun select(vararg fields: String): QueryBuilder<T> {
        // QueryDSL select with specific fields would change the query type
        // For entity queries, we keep selecting the full entity
        // Custom projection would require a different approach
        return this
    }

    override fun addSelect(vararg fields: String): QueryBuilder<T> {
        // Similar to select, entity queries return the full entity
        return this
    }

    override fun limit(limit: Int): QueryBuilder<T> {
        query.limit(limit.toLong())
        return this
    }

    override fun offset(offset: Long): QueryBuilder<T> {
        query.offset(offset)
        return this
    }

    override fun get(): List<T> {
        return query.fetch()
    }

    override fun page(limit: Int, offset: Long): Page<T> {
        // Count total first before applying limit/offset
        val total = query.fetch().size.toLong()
        
        // Then apply pagination and fetch content
        val content = query.limit(limit.toLong()).offset(offset).fetch()
        
        return Page(
            content = content,
            totalElements = total,
            limit = limit,
            offset = offset
        )
    }

    override fun findFirst(): Optional<T> {
        val result = query.fetchFirst()
        @Suppress("UNCHECKED_CAST")
        return Optional.ofNullable(result) as Optional<T>
    }

    override fun getWhereConditions(): Any {
        return predicates
    }

    /**
     * Get the underlying QueryDSL query for advanced operations
     */
    fun getQuery(): JPAQuery<T> {
        return query
    }

    /**
     * Get a path expression for a field name with global caching.
     * 
     * Uses reflection to retrieve the field from the Q-class on first access,
     * then caches the result globally for all future query instances. This provides
     * excellent performance in server applications where the same entities are queried
     * repeatedly.
     * 
     * The cache key combines entity type and field name to prevent collisions between
     * different entity types that may have fields with the same name.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getPath(field: String): Path<*> {
        val cacheKey = entityPath.type to field
        
        // Use computeIfAbsent for atomic get-or-create operation
        return globalPathCache.computeIfAbsent(cacheKey) {
            // Try to get the field from the entity path using reflection
            try {
                val pathField = entityPath::class.java.getDeclaredField(field)
                pathField.isAccessible = true
                pathField.get(entityPath) as Path<*>
            } catch (e: NoSuchFieldException) {
                // If field doesn't exist, create a simple path using PathBuilder
                PathBuilder(entityPath.type, entityPath.metadata).get(field)
            }
        }
    }

    /**
     * Create a predicate based on the operator and value
     */
    @Suppress("UNCHECKED_CAST")
    private fun createPredicate(path: Path<*>, operator: BaradumOperator, value: Any?): Predicate {
        return when (operator) {
            BaradumOperator.EQUAL -> createEqualPredicate(path, value)
            BaradumOperator.DIFF -> createNotEqualPredicate(path, value)
            BaradumOperator.GREATER -> createGreaterPredicate(path, value)
            BaradumOperator.GREATER_OR_EQUAL -> createGreaterOrEqualPredicate(path, value)
            BaradumOperator.LESS -> createLessPredicate(path, value)
            BaradumOperator.LESS_OR_EQUAL -> createLessOrEqualPredicate(path, value)
            BaradumOperator.LIKE -> createLikePredicate(path, value)
            BaradumOperator.NOT_LIKE -> createNotLikePredicate(path, value)
            BaradumOperator.IN -> createInPredicate(path, value)
            BaradumOperator.NOT_IN -> createNotInPredicate(path, value)
            BaradumOperator.IS_NULL -> createIsNullPredicate(path)
            BaradumOperator.IS_NOT_NULL -> createIsNotNullPredicate(path)
            BaradumOperator.BETWEEN -> createBetweenPredicate(path, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createEqualPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is StringPath -> path.eq(value as String?)
            is NumberPath<*> -> (path as NumberPath<Comparable<Any>>).eq(value as Comparable<Any>?)
            is BooleanPath -> path.eq(value as Boolean?)
            is DatePath<*> -> (path as DatePath<Comparable<Any>>).eq(value as Comparable<Any>?)
            is DateTimePath<*> -> (path as DateTimePath<Comparable<Any>>).eq(value as Comparable<Any>?)
            is ComparablePath<*> -> (path as ComparablePath<Comparable<Any>>).eq(value as Comparable<Any>?)
            else -> (path as SimpleExpression<Any>).eq(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createNotEqualPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is StringPath -> path.ne(value as String?)
            is NumberPath<*> -> (path as NumberPath<Comparable<Any>>).ne(value as Comparable<Any>?)
            is BooleanPath -> path.ne(value as Boolean?)
            is DatePath<*> -> (path as DatePath<Comparable<Any>>).ne(value as Comparable<Any>?)
            is DateTimePath<*> -> (path as DateTimePath<Comparable<Any>>).ne(value as Comparable<Any>?)
            is ComparablePath<*> -> (path as ComparablePath<Comparable<Any>>).ne(value as Comparable<Any>?)
            else -> (path as SimpleExpression<Any>).ne(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createGreaterPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is NumberPath<*> -> {
                when (value) {
                    is Number -> Expressions.predicate(Ops.GT, path as Expression<*>, Expressions.constant(value))
                    else -> throw IllegalArgumentException("Cannot compare number field with non-number value")
                }
            }
            is DatePath<*> -> Expressions.predicate(Ops.GT, path as Expression<*>, Expressions.constant(value))
            is DateTimePath<*> -> Expressions.predicate(Ops.GT, path as Expression<*>, Expressions.constant(value))
            is ComparablePath<*> -> Expressions.predicate(Ops.GT, path as Expression<*>, Expressions.constant(value))
            else -> Expressions.predicate(Ops.GT, path as Expression<*>, Expressions.constant(value))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createGreaterOrEqualPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is NumberPath<*> -> {
                when (value) {
                    is Number -> Expressions.predicate(Ops.GOE, path as Expression<*>, Expressions.constant(value))
                    else -> throw IllegalArgumentException("Cannot compare number field with non-number value")
                }
            }
            is DatePath<*> -> Expressions.predicate(Ops.GOE, path as Expression<*>, Expressions.constant(value))
            is DateTimePath<*> -> Expressions.predicate(Ops.GOE, path as Expression<*>, Expressions.constant(value))
            is ComparablePath<*> -> Expressions.predicate(Ops.GOE, path as Expression<*>, Expressions.constant(value))
            else -> Expressions.predicate(Ops.GOE, path as Expression<*>, Expressions.constant(value))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createLessPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is NumberPath<*> -> {
                when (value) {
                    is Number -> Expressions.predicate(Ops.LT, path as Expression<*>, Expressions.constant(value))
                    else -> throw IllegalArgumentException("Cannot compare number field with non-number value")
                }
            }
            is DatePath<*> -> Expressions.predicate(Ops.LT, path as Expression<*>, Expressions.constant(value))
            is DateTimePath<*> -> Expressions.predicate(Ops.LT, path as Expression<*>, Expressions.constant(value))
            is ComparablePath<*> -> Expressions.predicate(Ops.LT, path as Expression<*>, Expressions.constant(value))
            else -> Expressions.predicate(Ops.LT, path as Expression<*>, Expressions.constant(value))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createLessOrEqualPredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is NumberPath<*> -> {
                when (value) {
                    is Number -> Expressions.predicate(Ops.LOE, path as Expression<*>, Expressions.constant(value))
                    else -> throw IllegalArgumentException("Cannot compare number field with non-number value")
                }
            }
            is DatePath<*> -> Expressions.predicate(Ops.LOE, path as Expression<*>, Expressions.constant(value))
            is DateTimePath<*> -> Expressions.predicate(Ops.LOE, path as Expression<*>, Expressions.constant(value))
            is ComparablePath<*> -> Expressions.predicate(Ops.LOE, path as Expression<*>, Expressions.constant(value))
            else -> Expressions.predicate(Ops.LOE, path as Expression<*>, Expressions.constant(value))
        }
    }

    private fun createLikePredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is StringPath -> path.like(value as String)
            else -> throw IllegalArgumentException("LIKE operator can only be used with String fields")
        }
    }

    private fun  createNotLikePredicate(path: Path<*>, value: Any?): Predicate {
        return when (path) {
            is StringPath -> path.notLike(value as String)
            else -> throw IllegalArgumentException("NOT LIKE operator can only be used with String fields")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createInPredicate(path: Path<*>, value: Any?): Predicate {
        val values = when (value) {
            is Collection<*> -> value
            is Array<*> -> value.toList()
            else -> listOf(value)
        }
        
        return when (path) {
            is StringPath -> path.`in`(values as Collection<String>)
            is NumberPath<*> -> (path as NumberPath<Comparable<Any>>).`in`(values as Collection<Comparable<Any>>)
            is BooleanPath -> path.`in`(values as Collection<Boolean>)
            is ComparablePath<*> -> (path as ComparablePath<Comparable<Any>>).`in`(values as Collection<Comparable<Any>>)
            else -> (path as SimpleExpression<Any>).`in`(values)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createNotInPredicate(path: Path<*>, value: Any?): Predicate {
        val values = when (value) {
            is Collection<*> -> value
            is Array<*> -> value.toList()
            else -> listOf(value)
        }
        
        return when (path) {
            is StringPath -> path.notIn(values as Collection<String>)
            is NumberPath<*> -> (path as NumberPath<Comparable<Any>>).notIn(values as Collection<Comparable<Any>>)
            is BooleanPath -> path.notIn(values as Collection<Boolean>)
            is ComparablePath<*> -> (path as ComparablePath<Comparable<Any>>).notIn(values as Collection<Comparable<Any>>)
            else -> (path as SimpleExpression<Any>).notIn(values)
        }
    }

    private fun createIsNullPredicate(path: Path<*>): Predicate {
        return when (path) {
            is StringPath -> path.isNull
            is NumberPath<*> -> path.isNull
            is BooleanPath -> path.isNull
            is DatePath<*> -> path.isNull
            is DateTimePath<*> -> path.isNull
            is ComparablePath<*> -> path.isNull
            else -> (path as SimpleExpression<*>).isNull
        }
    }

    private fun createIsNotNullPredicate(path: Path<*>): Predicate {
        return when (path) {
            is StringPath -> path.isNotNull
            is NumberPath<*> -> path.isNotNull
            is BooleanPath -> path.isNotNull
            is DatePath<*> -> path.isNotNull
            is DateTimePath<*> -> path.isNotNull
            is ComparablePath<*> -> path.isNotNull
            else -> (path as SimpleExpression<*>).isNotNull
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createBetweenPredicate(path: Path<*>, value: Any?): Predicate {
        // Expect value to be a Pair or array of two elements
        val (from, to) = when (value) {
            is Pair<*, *> -> value.first to value.second
            is Array<*> -> value[0] to value[1]
            is List<*> -> value[0] to value[1]
            else -> throw IllegalArgumentException("BETWEEN operator requires a pair of values")
        }
        
        return Expressions.predicate(
            Ops.BETWEEN,
            path as Expression<*>,
            Expressions.constant(from),
            Expressions.constant(to)
        )
    }

    /**
     * Apply all predicates to the query based on the where operator
     */
    private fun applyPredicates() {
        if (predicates.isEmpty()) return
        
        val combinedPredicate = when (currentWhereOperator) {
            WhereOperator.AND -> {
                predicates.reduce { acc, predicate -> (acc as BooleanExpression).and(predicate) }
            }
            WhereOperator.OR -> {
                predicates.reduce { acc, predicate -> (acc as BooleanExpression).or(predicate) }
            }
        }
        
        query.where(combinedPredicate)
    }
}
