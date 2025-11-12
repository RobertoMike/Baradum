package io.github.robertomike.baradum.core.interfaces

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.models.Page
import java.util.Optional

/**
 * Core interface for building queries in a provider-agnostic way
 */
interface QueryBuilder<T> {
    /**
     * Add a where condition to the query
     */
    fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator = WhereOperator.AND): QueryBuilder<T>
    
    /**
     * Add a simple equality where condition
     */
    fun where(field: String, value: Any?): QueryBuilder<T> {
        return where(field, BaradumOperator.EQUAL, value)
    }
    
    /**
     * Add an order by clause
     */
    fun orderBy(field: String, direction: SortDirection = SortDirection.ASC): QueryBuilder<T>
    
    /**
     * Set select fields (if supported)
     */
    fun select(vararg fields: String): QueryBuilder<T>
    
    /**
     * Add select fields to existing selection
     */
    fun addSelect(vararg fields: String): QueryBuilder<T>
    
    /**
     * Set the limit for results
     */
    fun limit(limit: Int): QueryBuilder<T>
    
    /**
     * Set the offset for results
     */
    fun offset(offset: Long): QueryBuilder<T>
    
    /**
     * Execute the query and return all results
     */
    fun get(): List<T>
    
    /**
     * Execute the query and return paginated results
     */
    fun page(limit: Int, offset: Long): Page<T>
    
    /**
     * Execute the query and return the first result if any
     */
    fun findFirst(): Optional<T>
    
    /**
     * Get access to provider-specific where conditions (if needed)
     */
    fun getWhereConditions(): Any?
}
