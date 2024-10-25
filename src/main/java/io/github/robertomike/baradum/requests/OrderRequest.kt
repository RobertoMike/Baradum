package io.github.robertomike.baradum.requests

import io.github.robertomike.hefesto.enums.Sort

data class OrderRequest @JvmOverloads constructor(val field: String? = null, val sort: Sort = Sort.ASC)
