package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.actions.Select;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.utils.Page;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@CustomTest
public class SelectTest {

    @Test
    void selects(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .selects("name", "email");

        verify(hefesto).select("name", "email");
    }

    @Test
    void addSelects(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .addSelects("name", "email");

        verify(hefesto).addSelect("name");
        verify(hefesto).addSelect("email");
    }

    @Test
    void addSelectsObject(Hefesto<User> hefesto) {
        var select = new Select("name");

        Baradum.make(User.class)
                .addSelects(select);

        verify(hefesto).addSelect(select);
    }
}
