package io.github.robertomike.baradum.querydsl.converters

import io.github.robertomike.baradum.core.enums.SortDirection
import com.querydsl.core.types.Order

/**
 * Converter between Baradum sort directions and QueryDSL orders
 */
object SortConverter {
    fun toQueryDsl(direction: SortDirection): Order {
        return when (direction) {
            SortDirection.ASC -> Order.ASC
            SortDirection.DESC -> Order.DESC
        }
    }

    fun fromQueryDsl(order: Order): SortDirection {
        return when (order) {
            Order.ASC -> SortDirection.ASC
            Order.DESC -> SortDirection.DESC
        }
    }
}
