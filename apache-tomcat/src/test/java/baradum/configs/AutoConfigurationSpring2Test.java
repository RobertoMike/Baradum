package baradum.configs;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.AutoConfigurationSpring2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AutoConfigurationSpring2Test {

    @Test
    void constructor() throws NoSuchFieldException, IllegalAccessException {
        new AutoConfigurationSpring2(mock(HttpServletRequest.class));

        var field = Baradum.class.getDeclaredField("request");
        field.setAccessible(true);

        assertNotNull(field.get(null));
    }
}