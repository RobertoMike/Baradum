package io.github.robertomike.baradum.utils

fun <T: Enum<T>> Class<T>.valueOf(type: String): T =
    java.lang.Enum.valueOf(this, type)