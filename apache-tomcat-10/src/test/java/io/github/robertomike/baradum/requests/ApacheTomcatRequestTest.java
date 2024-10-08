package io.github.robertomike.baradum.requests;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        var body = apacheTomcatRequest.loadBodyAndGet();

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
    void cleanBody() {
        apacheTomcatRequest.setBodyRequest(new BodyRequest());

        apacheTomcatRequest.cleanBody();

        assertNull(apacheTomcatRequest.getBodyRequest());
    }

    @Test
    void getReader() throws IOException {
        var reader = mock(BufferedReader.class);

        when(request.getReader()).thenReturn(reader);

        var readerResult = apacheTomcatRequest.getReader();

        assertNotNull(readerResult);
        assertEquals(reader, readerResult);
    }

    @Test
    void findByName() {
        when(request.getParameter("name")).thenReturn("value");

        var result = apacheTomcatRequest.findByName("name");

        assertNotNull(result);
        assertEquals("value", result);
    }
}