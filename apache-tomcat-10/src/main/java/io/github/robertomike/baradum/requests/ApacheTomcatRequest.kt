package io.github.robertomike.baradum.requests;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;

public class ApacheTomcatRequest extends BasicRequest<HttpServletRequest> {
    public ApacheTomcatRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String findParamByName(String name) {
        return getRequest().getParameter(name);
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return getRequest().getReader();
    }
}
