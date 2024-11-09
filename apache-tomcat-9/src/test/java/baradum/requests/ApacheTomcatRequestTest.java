package baradum.requests;

import io.github.robertomike.baradum.requests.ApacheTomcatRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApacheTomcatRequestTest {
    @Mock
    HttpServletRequest request;
    @InjectMocks
    ApacheTomcatRequest apacheTomcatRequest;

    @Test
    void loadBody() throws IOException {
        var reader = mock(BufferedReader.class);

        when(request.getReader()).thenReturn(reader);
        when(reader.lines()).thenReturn(Stream.of("{\"filters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"},{\"field\":\"name\",\"value\":\"abc%\",\"operator\":\"LIKE\",\"type\":\"OR\"},{\"subFilters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"},{\"field\":\"name\",\"value\":\"abc%\",\"operator\":\"LIKE\",\"type\":\"OR\"}]}],\"sorts\":[{\"field\":\"id\"},{\"field\":\"name\",\"sort\":\"DESC\"}]}"));

        var body = apacheTomcatRequest.getBody();

        assertNotNull(body);
        assertNotNull(body.getFilters());
        assertFalse(body.getFilters().isEmpty());
        assertFalse(body.getSorts().isEmpty());
    }

    @Test
    void findParamByName() {
        when(request.getParameter("name")).thenReturn("value");

        assertEquals("value", apacheTomcatRequest.findParamByName("name"));
    }

    @Test
    void notExistsByName() {
        assertTrue(apacheTomcatRequest.notExistsByName("name"));
    }

    @Test
    void existsByName() {
        when(request.getParameter("name")).thenReturn("value");

        assertFalse(apacheTomcatRequest.notExistsByName("name"));
    }

    @Test
    void getMethod() {
        when(request.getMethod()).thenReturn("GET");

        assertEquals("GET", apacheTomcatRequest.getMethod());
    }

    @Test
    void getReader() throws IOException {
        var reader = mock(BufferedReader.class);

        when(reader.lines()).thenReturn(Stream.of("{}"));
        when(request.getReader()).thenReturn(reader);

        var readerResult = apacheTomcatRequest.getJson();

        assertNotNull(readerResult);
        assertEquals("{}", readerResult);
    }
}