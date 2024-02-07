package io.github.robertomike.baradum.configs;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.requests.ApacheTomcatRequest;
import jakarta.servlet.http.HttpServletRequest;

public class AutoConfigurationSpring3 {
    public AutoConfigurationSpring3(HttpServletRequest request) {
        Baradum.setRequest(new ApacheTomcatRequest(request));
    }
}
