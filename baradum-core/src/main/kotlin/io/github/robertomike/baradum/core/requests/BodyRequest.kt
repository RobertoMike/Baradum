package io.github.robertomike.baradum.core.requests

data class BodyRequest @JvmOverloads constructor(
    var filters: List<FilterRequest> = ArrayList(),
    var sorts: List<OrderRequest> = ArrayList()
)
