package io.github.robertomike.baradum.configs

import io.github.robertomike.baradum.Baradum
import io.github.robertomike.baradum.requests.ApacheTomcatRequest
import javax.servlet.http.HttpServletRequest

class AutoConfigurationSpring2(request: HttpServletRequest) {
    init {
        Baradum.request = ApacheTomcatRequest(request)
    }
}
