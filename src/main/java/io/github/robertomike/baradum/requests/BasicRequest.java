package io.github.robertomike.baradum.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class BasicRequest<T> {
    private ObjectMapper mapper = new ObjectMapper();
    private final T request;
    private BodyRequest bodyRequest = null;

    /**
     * Finds a parameter by name.
     *
     * @param name the name of the parameter to find
     * @return the parameter found by name
     */
    public String findByName(String name) {
        return findParamByName(name);
    }

    /**
     * Checks if a parameter with the given name does not exist.
     *
     * @param name the name of the parameter to check
     * @return true if the parameter does not exist, false otherwise
     */
    public boolean notExistsByName(String name) {
        var value = findParamByName(name);

        return value == null || value.isBlank();
    }

    /**
     * Finds a parameter by its name.
     *
     * @param name the name of the parameter
     * @return the parameter found, or
     */
    public abstract String findParamByName(String name);

    public abstract String getMethod();

    public abstract BufferedReader getReader() throws IOException;

    @SneakyThrows
    public void loadBody() {
        if (bodyRequest == null) {
            bodyRequest = mapper.readValue(
                    getReader().lines().collect(Collectors.joining(System.lineSeparator())),
                    BodyRequest.class
            );
        }
    }

    public void cleanBody() {
        bodyRequest = null;
    }
}
