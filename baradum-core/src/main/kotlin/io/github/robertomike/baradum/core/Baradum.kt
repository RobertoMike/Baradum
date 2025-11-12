package io.github.robertomike.baradum.core

import io.github.robertomike.baradum.core.exceptions.BaradumException
import io.github.robertomike.baradum.core.filters.Filter
import io.github.robertomike.baradum.core.filters.Filterable
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.interfaces.QueryBuilderProvider
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.core.requests.BasicRequest
import io.github.robertomike.baradum.core.sorting.OrderBy
import io.github.robertomike.baradum.core.sorting.Sortable
import java.util.Optional
import java.util.ServiceLoader
import java.util.function.Consumer

/**
 * Core Baradum class - provider agnostic
 * 
 * Can be instantiated directly with a QueryBuilder, or via the make() factory method
 * which uses Java's ServiceLoader to automatically discover available QueryBuilder implementations.
 */
class Baradum<T, Q : QueryBuilder<T>>(
    private val queryBuilder: Q
) {
    private var sortable = Sortable<Q>()
    private var filterable = Filterable<Q>()
    private var useBody = false
    private var onlyBody = false
    private var instanceParams: Map<String, String>? = null

    companion object {
        private val providers: List<QueryBuilderProvider> by lazy {
            ServiceLoader.load(QueryBuilderProvider::class.java).toList()
        }
        
        /**
         * Create a Baradum instance using the first available QueryBuilderProvider
         * discovered via Java's ServiceLoader mechanism.
         * 
         * @param modelClass The model class to query
         * @return Baradum instance with the discovered QueryBuilder
         * @throws BaradumException if no QueryBuilderProvider is found
         */
        @JvmStatic
        fun <T, Q : QueryBuilder<T>> make(modelClass: Class<T>): Baradum<T, Q> {
            val provider = providers.firstOrNull { it.supports(modelClass) }
                ?: throw BaradumException(
                    "No QueryBuilderProvider found. Make sure you have a query builder module " +
                    "(e.g., baradum-hefesto) in your classpath and it's properly registered via ServiceLoader."
                )
            
            @Suppress("UNCHECKED_CAST")
            return Baradum(provider.create(modelClass) as Q)
        }
        
        /**
         * Create a Baradum instance using a specific QueryBuilderProvider by name
         * 
         * @param modelClass The model class to query
         * @param providerName The name of the provider to use (e.g., "hefesto")
         * @return Baradum instance with the specified QueryBuilder
         * @throws BaradumException if the named provider is not found
         */
        @JvmStatic
        fun <T, Q : QueryBuilder<T>> make(modelClass: Class<T>, providerName: String): Baradum<T, Q> {
            val provider = providers.firstOrNull { it.getName() == providerName }
                ?: throw BaradumException(
                    "QueryBuilderProvider '$providerName' not found. Available providers: " +
                    providers.joinToString { it.getName() }
                )
            
            @Suppress("UNCHECKED_CAST")
            return Baradum(provider.create(modelClass) as Q) as Baradum<T, Q>
        }
        
        /**
         * Global request for Baradum to resolve params and body
         * @deprecated Consider using withParams() for instance-level parameters
         */
        @JvmStatic 
        var request: BasicRequest<out Any>? = null
    }

    /**
     * Set parameters for this instance, eliminating need for global request
     */
    fun withParams(params: Map<String, String>): Baradum<T, Q> {
        this.instanceParams = params
        return this
    }

    /**
     * Convenience method for individual parameters
     */
    fun withParam(key: String, value: String): Baradum<T, Q> {
        val mutableParams = instanceParams?.toMutableMap() ?: mutableMapOf()
        mutableParams[key] = value
        this.instanceParams = mutableParams
        return this
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     */
     fun allowedFilters(vararg filters: Filter<*, *>): Baradum<T, Q> {
        filterable.addFilters(*filters)
        return this
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     */
    fun allowedFilters(filters: List<Filter<*, *>>): Baradum<T, Q> {
        filterable.addFilters(filters)
        return this
    }

    /**
     * Add allowed sort criteria to the Baradum object.
     */
    fun allowedSort(vararg sorts: String): Baradum<T, Q> {
        sortable.addSorts(*sorts)
        return this
    }

    /**
     * Add allowed sorts to the Baradum object.
     */
    fun allowedSort(vararg sorts: OrderBy): Baradum<T, Q> {
        sortable.addSorts(*sorts)
        return this
    }

    /**
     * Add allowed sorts to the Baradum object.
     */
    fun allowedSort(sorts: List<OrderBy>): Baradum<T, Q> {
        sortable.addSorts(sorts)
        return this
    }

    /**
     * Reset the list of select and set the passed selects
     */
    fun selects(vararg selects: String): Baradum<T, Q> {
        queryBuilder.select(*selects)
        return this
    }

    /**
     * Add new select to the current list
     */
    fun addSelects(vararg selects: String): Baradum<T, Q> {
        queryBuilder.addSelect(*selects)
        return this
    }

    /**
     * Sets the value of the `useBody` variable to `true` and returns the current instance.
     * Using this method allows you to use this class with POST requests and at the same time request params for GET
     */
    fun useBody(): Baradum<T, Q> {
        useBody = true
        return this
    }

    /**
     * Sets the value of the `useBody` variable to `true`, onlyBody to `true` and returns the current instance.
     * Using this method only allows you to use this class with POST requests.
     */
    fun useOnlyBody(): Baradum<T, Q> {
        useBody = true
        onlyBody = true
        return this
    }

    /**
     * Apply the filters and sorts based on priority: instance params > body > global request
     */
    private fun apply() {
        when {
            instanceParams != null -> {
                filterable.apply(queryBuilder, instanceParams!!)
                sortable.apply(queryBuilder, instanceParams!!)
            }
            useBody && request != null && request!!.isPost() -> {
                if (onlyBody && !request!!.isPost()) {
                    throw BaradumException("Body can only be used with POST requests")
                }
                val body = request!!.getBody()
                filterable.apply(queryBuilder, body.filters)
                sortable.apply(queryBuilder, body.sorts)
            }
            request != null -> {
                if (onlyBody) {
                    throw BaradumException("Body can only be used with POST requests")
                }
                filterable.apply(queryBuilder, request!!)
                sortable.apply(queryBuilder, request!!)
            }
        }
    }

    /**
     * Get the list of type T.
     */
    fun get(): List<T> {
        apply()
        return queryBuilder.get()
    }

    /**
     * Retrieve a page of elements.
     */
    fun page(limit: Int, offset: Long): Page<T> {
        apply()
        
        // Extract limit and offset from params if provided
        val actualLimit = instanceParams?.get("limit")?.toIntOrNull() ?: limit
        val actualOffset = instanceParams?.get("offset")?.toLongOrNull() ?: offset
        
        return queryBuilder.page(actualLimit, actualOffset)
    }

    /**
     * Returns a page of items with the specified limit.
     */
    fun page(limit: Int): Page<T> {
        return page(limit, 0)
    }

    /**
     * Returns an optional with a single element.
     */
    fun findFirst(): Optional<T> {
        apply()
        return queryBuilder.findFirst()
    }

    /**
     * Get access to where conditions (provider-specific)
     */
    fun getWhereConditions(): Any? {
        return queryBuilder.getWhereConditions()
    }

    /**
     * Access the underlying query builder for advanced operations
     */
    fun builder(lambda: Consumer<Q>): Baradum<T, Q> {
        lambda.accept(queryBuilder)
        return this
    }

    /**
     * Get direct access to the query builder
     */
    fun getBuilder(): Q {
        return queryBuilder
    }
}
