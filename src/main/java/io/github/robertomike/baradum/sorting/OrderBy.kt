package io.github.robertomike.baradum.sorting

@JvmRecord
data class OrderBy @JvmOverloads constructor(val name: String, val internalName: String = name)
