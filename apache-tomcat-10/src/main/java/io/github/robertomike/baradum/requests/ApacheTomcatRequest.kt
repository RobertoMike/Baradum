package io.github.robertomike.baradum.requests

import jakarta.servlet.http.HttpServletRequest
import java.util.stream.Collectors

class ApacheTomcatRequest(request: HttpServletRequest) : BasicRequest<HttpServletRequest>(request) {
    override fun findParamByName(name: String): String? {
        return request.getParameter(name)
    }

    override val method: String
        get() = request.method

    override val json: String
        get() = request.reader.lines().collect(Collectors.joining(System.lineSeparator()))
}
