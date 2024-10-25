package io.github.robertomike.baradum.requests

data class BodyRequest @JvmOverloads constructor(
    var filters: List<FilterRequest> = ArrayList(),
    var sorts: List<OrderRequest> = ArrayList()
)
