package io.github.robertomike.baradum

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.core.interfaces.QueryBuilder

/**
 * Factory for creating Baradum instances with Apache Tomcat backend
 */
object Baradum {
    /**
     * Creates a new Baradum instance with the specified query builder
     */
    @JvmStatic
    fun <T, Q : QueryBuilder<T>> make(queryBuilder: Q): Baradum<T, Q> {
        return Baradum(queryBuilder)
    }
}
