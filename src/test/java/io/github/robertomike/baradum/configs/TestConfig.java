package io.github.robertomike.baradum.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.constructors.ConstructWhereImplementation;
import org.junit.jupiter.api.extension.*;
import org.mockito.MockedStatic;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestConfig implements BeforeAllCallback, BeforeEachCallback, ExtensionContext.Store.CloseableResource, ParameterResolver {

    /**
     * gatekeeper to prevent multiple Threads within the same routine
     */
    private static final Lock LOCK = new ReentrantLock();
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.GLOBAL;
    private static MockedStatic<Hefesto> hefestoMockedStatic;
    private static Hefesto<?> hefesto;
    private static ConstructWhereImplementation constructWhereImplementation;
    private static BasicRequest<?> basicRequest;
    private final ObjectMapper mapper = new ObjectMapper();
    /**
     * volatile boolean to tell other threads, when unblocked, whether they should try attempt start-up.  Alternatively, could use AtomicBoolean.
     */
    private static volatile boolean started = false;

    @Override
    public void beforeAll(final ExtensionContext context) {
        // lock the access so only one Thread has access to it
        LOCK.lock();
        try {
            if (!started) {
                started = true;

                hefestoMockedStatic = mockStatic(Hefesto.class);
                hefesto = mock(Hefesto.class);
                basicRequest = mock(BasicRequest.class);
                constructWhereImplementation = mock(ConstructWhereImplementation.class);

                hefestoMockedStatic.when(() -> Hefesto.make(any())).thenReturn(hefesto);

                // Store the mock in the extension context for later use in tests
                Baradum.setRequest(basicRequest);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // free the access
            LOCK.unlock();
        }

        var globalStore = context.getStore(NAMESPACE);
        globalStore.put("BasicRequest", basicRequest);
        globalStore.put("Hefesto", hefesto);
        globalStore.put("ConstructWhereImplementation", constructWhereImplementation);
    }

    @Override
    public void close() {
        hefestoMockedStatic.close();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        return context.getStore(NAMESPACE)
                .get(getSimpleName(parameterContext)) != null;
    }

    private String getSimpleName(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType().getSimpleName();
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .get(getSimpleName(parameterContext));
    }

    public void initForHefesto() {
        when(hefesto.getWheres()).thenReturn(constructWhereImplementation);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws JsonProcessingException {
        reset(hefesto, basicRequest);
        initForHefesto();

        when(basicRequest.notExistsByName("sort")).thenReturn(true);
        when(basicRequest.getMethod()).thenReturn("GET");
        when(basicRequest.isPost()).thenReturn(false);

        var method = context.getRequiredTestMethod();
        if (method == null) {
            return;
        }

        for (var annotation : method.getAnnotations()) {
            if (annotation instanceof ParameterRequest parameter) {
                when(basicRequest.findByName(parameter.key())).thenReturn(parameter.value());
                when(basicRequest.notExistsByName(parameter.key())).thenReturn(false);
            }
            if (annotation instanceof BodyRequest body) {
                when(basicRequest.getMethod()).thenReturn("POST");
                when(basicRequest.isPost()).thenReturn(true);
                when(basicRequest.getBody()).thenReturn(mapper.readValue(
                        body.value(), io.github.robertomike.baradum.requests.BodyRequest.class
                ));
            }
        }
    }
}