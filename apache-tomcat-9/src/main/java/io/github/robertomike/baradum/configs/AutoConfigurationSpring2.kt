package io.github.robertomike.baradum.configs

import io.github.robertomike.baradum.Baradum.Companion.setRequest
import io.github.robertomike.baradum.requests.ApacheTomcatRequest
import javax.servlet.http.HttpServletRequest

class AutoConfigurationSpring2(request: HttpServletRequest) {
    init {
        setRequest(ApacheTomcatRequest(request))
    }
}
