package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.exceptions.BaradumException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.utils.Page;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CustomTest
public class BaradumTest {

    @Test
    void getAll(Hefesto<User> hefesto) {
        when(hefesto.get()).thenReturn(Collections.nCopies(10, mock(User.class)));

        var users = Baradum.make(User.class)
                .get();

        assertNotNull(users);
        assertEquals(10, users.size());
    }

    @Test
    void paginate(Hefesto<User> hefesto) {
        when(hefesto.page(5, 0)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 0, 10));

        var users = Baradum.make(User.class)
                .page(5);

        assertNotNull(users);
        assertEquals(5, users.getData().size());
        assertEquals(0, users.getPage());
    }

    @Test
    void paginateWithBothParameter(Hefesto<User> hefesto) {
        when(hefesto.page(5, 1)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 1, 10));

        var users = Baradum.make(User.class)
                .page(5, 1);

        assertNotNull(users);
        assertEquals(5, users.getData().size());
        assertEquals(1, users.getPage());
    }

    @Test
    void tryingToUseBodyWithParams() {
        assertThrows(BaradumException.class, () ->
                Baradum.make(User.class)
                        .useBody()
                        .get()
        );
    }
}
