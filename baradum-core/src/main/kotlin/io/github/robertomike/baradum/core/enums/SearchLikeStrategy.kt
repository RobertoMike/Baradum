package io.github.robertomike.baradum.core.enums

enum class SearchLikeStrategy {
    FINAL, // Only var %
    START, // Only % var
    COMPLETE; // Use % var %


    fun apply(value: String): String {
        return when (this) {
            FINAL -> "$value%"
            START -> "%$value"
            COMPLETE -> "%$value%"
        }
    }
}