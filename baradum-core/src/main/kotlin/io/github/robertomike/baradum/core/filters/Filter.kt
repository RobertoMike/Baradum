package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.requests.BasicRequest
import lombok.Getter
import kotlin.reflect.KProperty1

@Getter
abstract class Filter<T : Any, Q : QueryBuilder<*>>(val param: String, val internalName: String) {
    private var defaultValue: String? = null
    private var ignored: MutableList<String> = ArrayList()

    /**
     * Alternative constructor using Kotlin property reference for type-safe field access.
     * 
     * Usage:
     * ```kotlin
     * ExactFilter(User::name)  // Uses 'name' as both param and internalName
     * PartialFilter(User::email, "searchEmail")  // Uses 'email' as internalName, 'searchEmail' as param
     * ```
     * 
     * @param property Kotlin property reference (e.g., User::name)
     * @param param Optional custom parameter name (defaults to property name)
     */
    constructor(property: KProperty1<*, *>, param: String? = null) : this(
        param ?: property.name,
        property.name
    )

    fun addIgnore(vararg ignored: String): Filter<T, Q> {
        this.ignored.addAll(listOf(*ignored))
        return this
    }

    fun setDefaultValue(defaultValue: String?): Filter<T, Q> {
        this.defaultValue = defaultValue
        return this
    }

    fun ignore(value: String): Boolean {
        val trimmedValue = value.trim()
        return ignored.any { it == trimmedValue }
    }

    protected fun getOperator(value: String): BaradumOperator {
        if (value.contains("<=")) {
            return BaradumOperator.LESS_OR_EQUAL
        } else if (value.contains(">=")) {
            return BaradumOperator.GREATER_OR_EQUAL
        } else if (value.contains("<>")) {
            return BaradumOperator.DIFF
        } else if (value.contains(">")) {
            return BaradumOperator.GREATER
        } else if (value.contains("<")) {
            return BaradumOperator.LESS
        }

        return BaradumOperator.EQUAL
    }

    protected fun cleanValue(value: String): String {
        if (value.contains("<=")) {
            return value.replace("<=", "")
        } else if (value.contains(">=")) {
            return value.replace(">=", "")
        } else if (value.contains("<>")) {
            return value.replace("<>", "")
        } else if (value.contains(">")) {
            return value.replace(">", "")
        } else if (value.contains("<")) {
            return value.replace("<", "")
        }

        return value
    }

    abstract fun filterByParam(query: Q, value: String)

    open fun filterByParam(query: Q, request: BasicRequest<*>) {
        if (request.notExistsByName(param) && defaultValue == null) {
            return
        }

        var parameter = request.findByName(param)

        if (parameter == null) {
            parameter = defaultValue
        }

        if (parameter == null) {
            return
        }

        if (ignore(parameter)) {
            return
        }

        filterByParam(query, parameter)
    }

    /**
     * Apply filter using a body map directly instead of using global request.
     * This allows detached filtering without depending on request context.
     * 
     * @param query The query builder to apply the filter to
     * @param body Map containing the filter parameters
     */
    open fun filterByParam(query: Q, body: Map<String, Any?>) {
        val value = body[param]?.toString() ?: defaultValue

        if (value == null) {
            return
        }

        if (ignore(value)) {
            return
        }

        filterByParam(query, value)
    }

    @Suppress("UNCHECKED_CAST")
    open fun transform(value: String): T {
        return value as T
    }

    /**
     * Whether this filter supports being used with body operations (POST with JSON).
     * Most filters support this by default.
     */
    open fun supportBodyOperation(): Boolean {
        return true
    }
}
