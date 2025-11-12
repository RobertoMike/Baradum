package io.github.robertomike.baradum.hefesto.converters

import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.hefesto.enums.Sort

object SortConverter {
    fun toHefesto(direction: SortDirection): Sort {
        return when (direction) {
            SortDirection.ASC -> Sort.ASC
            SortDirection.DESC -> Sort.DESC
        }
    }

    fun fromHefesto(sort: Sort): SortDirection {
        return when (sort) {
            Sort.ASC -> SortDirection.ASC
            Sort.DESC -> SortDirection.DESC
        }
    }
}
