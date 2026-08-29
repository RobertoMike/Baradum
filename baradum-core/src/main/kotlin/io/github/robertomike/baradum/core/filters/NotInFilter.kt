package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.exceptions.FilterException
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import kotlin.reflect.KProperty1

/**
 * Generic filter for NOT IN operator with comma-separated values.
 * The mirror image of [InFilter].
 *
 * Usage examples:
 * - "1,2,3" - NOT IN (1,2,3)
 * - "active,pending" - NOT IN ('active','pending')
 */
open class NotInFilter : Filter<List<String>, QueryBuilder<*>> {

    private val delimiter: String

    @JvmOverloads
    constructor(param: String, internalName: String = param, delimiter: String = ",") : super(param, internalName) {
        this.delimiter = delimiter
    }

    /**
     * Type-safe constructor using Kotlin property reference
     */
    @JvmOverloads
    constructor(property: KProperty1<*, *>, param: String? = null, delimiter: String = ",") : super(property, param) {
        this.delimiter = delimiter
    }

    companion object {
        /**
         * Factory method for creating NotInFilter with KProperty
         */
        @JvmStatic
        fun of(property: KProperty1<*, *>): NotInFilter = NotInFilter(property)

        @JvmStatic
        fun of(property: KProperty1<*, *>, param: String): NotInFilter = NotInFilter(property, param)
    }

    /**
     * Split the value by delimiter and apply NOT IN operator.
     */
    override fun filterByParam(query: QueryBuilder<*>, value: String) {
        val values = transform(value)

        if (values.isEmpty()) {
            throw FilterException("Value list cannot be empty for NOT IN filter '$param'")
        }

        query.where(internalName, BaradumOperator.NOT_IN, values)
    }

    /**
     * Transform comma-separated string into list of trimmed values.
     */
    override fun transform(value: String): List<String> {
        return value.split(delimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
