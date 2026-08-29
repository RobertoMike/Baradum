package io.github.robertomike.baradum.core.filters

import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import java.util.function.BiConsumer

/**
 * Generic lambda-based custom filter, usable with any [QueryBuilder] backend.
 *
 * Usage:
 * ```kotlin
 * CustomFilter<QueryDslQueryBuilder<*>>("status") { query, value ->
 *     if (value == "premium") {
 *         query.where("subscription_level", BaradumOperator.GREATER, 5)
 *     } else {
 *         query.where("status", BaradumOperator.EQUAL, value)
 *     }
 * }
 * ```
 *
 * `baradum-hefesto` also ships its own [io.github.robertomike.baradum.hefesto.filters.CustomFilter]
 * pre-typed to `HefestoQueryBuilder`, which is slightly terser for Hefesto-only code - both work
 * identically, pick whichever fits.
 */
open class CustomFilter<Q : QueryBuilder<*>>(
    param: String,
    private val consumer: BiConsumer<Q, String>
) : Filter<Any, Q>(param, param) {

    override fun filterByParam(query: Q, value: String) {
        consumer.accept(query, value)
    }
}
