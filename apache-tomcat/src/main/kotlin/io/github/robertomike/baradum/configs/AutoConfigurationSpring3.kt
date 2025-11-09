package io.github.robertomike.baradum.configs

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.baradum.requests.ApacheTomcatRequest
import jakarta.servlet.http.HttpServletRequest

class AutoConfigurationSpring3(request: HttpServletRequest) {
    init {
        Baradum.request = ApacheTomcatRequest(request)
    }
}
