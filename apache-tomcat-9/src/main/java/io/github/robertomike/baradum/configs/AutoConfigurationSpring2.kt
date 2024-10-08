package io.github.robertomike.baradum.configs;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.requests.ApacheTomcatRequest;
import javax.servlet.http.HttpServletRequest;

public class AutoConfigurationSpring2 {
    public AutoConfigurationSpring2(HttpServletRequest request) {
        Baradum.setRequest(new ApacheTomcatRequest(request));
    }
}
