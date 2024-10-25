package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator
import lombok.Getter

@Getter
abstract class Filter<T>(val param: String, val internalName: String) {
    private var defaultValue: String? = null
    private var ignored: MutableList<String> = ArrayList()

    fun addIgnore(vararg ignored: String): Filter<T> {
        this.ignored.addAll(listOf(*ignored))
        return this
    }

    fun setDefaultValue(defaultValue: String?): Filter<T> {
        this.defaultValue = defaultValue
        return this
    }

    fun ignore(value: String): Boolean {
        val trimmedValue = value.trim()

        return ignored.any { it == trimmedValue }
    }

    protected fun getOperator(value: String): Operator {
        if (value.contains("<=")) {
            return Operator.LESS_OR_EQUAL
        } else if (value.contains(">=")) {
            return Operator.GREATER_OR_EQUAL
        } else if (value.contains("<>")) {
            return Operator.DIFF
        } else if (value.contains(">")) {
            return Operator.GREATER
        } else if (value.contains("<")) {
            return Operator.LESS
        }

        return Operator.EQUAL
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

    abstract fun filterByParam(query: Hefesto<*>, value: String)

    open fun filterByParam(query: Hefesto<*>, request: BasicRequest<*>) {
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

    @Suppress("UNCHECKED_CAST")
    open fun transform(value: String): T {
        return value as T
    }

    open fun supportBodyOperation(): Boolean {
        return false
    }
}
