package io.github.robertomike.baradum.core.exceptions

class BaradumException @JvmOverloads constructor(message: String = "", cause: Throwable? = null) :
    RuntimeException(message, cause)
