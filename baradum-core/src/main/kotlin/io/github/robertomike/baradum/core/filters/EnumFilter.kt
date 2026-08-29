package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic EnumFilter for filtering enum values.
 * Supports single value or comma-separated values (IN operator).
 *
 * Usage:
 * ```kotlin
 * val statusFilter = EnumFilter<Status, MyQueryBuilder>("status", Status::class.java)
 * // Incoming value: "ACTIVE" -> WHERE status = ACTIVE
 * // Incoming value: "ACTIVE,PENDING" -> WHERE status IN (ACTIVE, PENDING)
 *
 * // Type-safe with KProperty
 * EnumFilter(User::status, Status::class.java)
 * ```
 */
open class EnumFilter<E : Enum<E>, Q : QueryBuilder<*>> : Filter<E, QueryBuilder<*>> {

    private val enumClass: Class<E>

    @JvmOverloads
    constructor(param: String, internalName: String = param, enumClass: Class<E>) : super(param, internalName) {
        this.enumClass = enumClass
    }

    /**
     * Type-safe constructor using Kotlin property reference.
     * The enum class still has to be passed explicitly - it can't be inferred from a property reference alone.
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, enumClass: Class<E>, param: String? = null) : super(property, param) {
        this.enumClass = enumClass
    }

    companion object {
        /**
         * Factory method for creating EnumFilter with KProperty
         */
        @JvmStatic
        fun <E : Enum<E>> of(property: KProperty1<*, *>, enumClass: Class<E>): EnumFilter<E, QueryBuilder<*>> =
            EnumFilter(property, enumClass)

        @JvmStatic
        fun <E : Enum<E>> of(property: KProperty1<*, *>, enumClass: Class<E>, param: String): EnumFilter<E, QueryBuilder<*>> =
            EnumFilter(property, enumClass, param)
    }

    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        if (value.contains(",")) {
            // Multiple values - use IN operator
            val values = value.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map(this::transform)
                .toSet()

            if (values.isNotEmpty()) {
                query.where(internalName, BaradumOperator.IN, values)
            }
        } else {
            // Single value - use EQUAL operator
            query.where(internalName, BaradumOperator.EQUAL, transform(value))
        }
    }

    override fun transform(value: String): E {
        try {
            return java.lang.Enum.valueOf(enumClass, value.trim())
        } catch (e: IllegalArgumentException) {
            val allowed = enumClass.enumConstants.joinToString { it.name }
            throw FilterException("Invalid value '$value' for $param. Allowed values: $allowed")
        }
    }

    override fun supportBodyOperation(): Boolean {
        return true
    }
}
