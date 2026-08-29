package io.github.robertomike.baradum.configs

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.requests.ApacheTomcatRequest
import jakarta.servlet.http.HttpServletRequest

@Suppress("DEPRECATION")
class AutoConfigurationSpring3(request: HttpServletRequest) {
    init {
        // Deprecated in favor of withParams()/withParam(), but this is the one sanctioned use:
        // Spring injects a request-scoped proxy here, so assigning it once at bean-creation time
        // stays correct across concurrent requests.
        Baradum.request = ApacheTomcatRequest(request)
    }
}
