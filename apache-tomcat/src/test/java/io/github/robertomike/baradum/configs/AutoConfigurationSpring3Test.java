package io.github.robertomike.baradum.configs;

import io.github.robertomike.baradum.Baradum;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AutoConfigurationSpring3Test {

    @Test
    void constructor() throws NoSuchFieldException, IllegalAccessException {
        new AutoConfigurationSpring3(mock(HttpServletRequest.class));

        var field = Baradum.class.getDeclaredField("request");
        field.setAccessible(true);

        assertNotNull(field.get(null));
    }
}