package io.github.robertomike.baradum.core.requests

import io.github.robertomike.baradum.core.enums.SortDirection

data class OrderRequest @JvmOverloads constructor(
    val field: String? = null, 
    val sort: SortDirection = SortDirection.ASC
)
