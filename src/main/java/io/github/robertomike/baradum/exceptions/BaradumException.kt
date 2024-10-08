package io.github.robertomike.baradum.exceptions

class BaradumException @JvmOverloads constructor(message: String = "", cause: Throwable? = null) :
    RuntimeException(message, cause)
