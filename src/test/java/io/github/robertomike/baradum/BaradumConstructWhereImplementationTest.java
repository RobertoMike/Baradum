package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.exceptions.BaradumException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.constructors.ConstructWhereImplementation;
import io.github.robertomike.hefesto.utils.Page;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@CustomTest
public class BaradumConstructWhereImplementationTest {
    @Test
    void where(Hefesto<User> hefesto, ConstructWhereImplementation construct) {
        when(hefesto.findFirst()).thenReturn(Optional.of(mock(User.class)));

        var users = Baradum.make(User.class)
                .where("id", 1L)
                .findFirst();

        verify(construct).add(any(Where.class));
        assertTrue(users.isPresent());
    }
}
