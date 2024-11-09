package io.github.robertomike.baradum.requests

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.robertomike.baradum.exceptions.BaradumException
import java.io.BufferedReader
import java.io.IOException
import java.util.stream.Collectors

abstract class BasicRequest<T>(val request: T) {
    private val mapper = ObjectMapper()

    /**
     * Finds a parameter by name.
     *
     * @param name the name of the parameter to find
     * @return the parameter found by name
     */
    fun findByName(name: String): String? {
        return findParamByName(name)
    }

    /**
     * Checks if a parameter with the given name does not exist.
     *
     * @param name the name of the parameter to check
     * @return true if the parameter does not exist, false otherwise
     */
    fun notExistsByName(name: String): Boolean {
        val value = findParamByName(name)

        return value.isNullOrBlank()
    }

    /**
     * Finds a parameter by its name.
     *
     * @param name the name of the parameter
     * @return the parameter found, or
     */
    abstract fun findParamByName(name: String): String?

    abstract val method: String

    fun isPost(): Boolean {
        return method == "POST"
    }

    abstract val json: String

    fun getBody(): BodyRequest {
        try {
            return mapper.readValue(json, BodyRequest::class.java)
        } catch (e: Exception) {
            throw BaradumException("Error reading body request", e)
        }
    }
}
