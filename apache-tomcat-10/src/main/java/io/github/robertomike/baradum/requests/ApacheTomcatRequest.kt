package io.github.robertomike.baradum.requests

import jakarta.servlet.http.HttpServletRequest
import java.io.BufferedReader
import java.io.IOException

class ApacheTomcatRequest(request: HttpServletRequest) : BasicRequest<HttpServletRequest>(request) {
    override fun findParamByName(name: String): String? {
        return request.getParameter(name)
    }

    override val method: String
        get() = request.method

    @get:Throws(IOException::class)
    override val reader: BufferedReader
        get() = request.reader
}
