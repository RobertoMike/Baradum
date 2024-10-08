package io.github.robertomike.baradum.configs

import io.github.robertomike.baradum.Baradum.Companion.setRequest
import io.github.robertomike.baradum.requests.ApacheTomcatRequest
import jakarta.servlet.http.HttpServletRequest

class AutoConfigurationSpring3(request: HttpServletRequest) {
    init {
        setRequest(ApacheTomcatRequest(request))
    }
}
