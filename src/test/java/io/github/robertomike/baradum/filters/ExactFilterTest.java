package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@CustomTest
public class ExactFilterTest {
    @ParameterRequest(key = "id", value = "1")
    void allowExactFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id")
                .get();

        verify(hefesto).where("id", "1");
    }

    @ParameterRequest(key = "name", value = "1")
    void ignoreNotAllowedValue(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id")
                .get();

        verify(hefesto, never()).where("name", "1");
    }

    @ParameterRequest(key = "alias", value = "1")
    void allowExactFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new ExactFilter("alias", "id"))
                .get();

        verify(hefesto).where("id", "1");
    }

    @Test
    void allowExactFilterWithDefault(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new ExactFilter("id").setDefaultValue("1"))
                .get();

        verify(hefesto).where("id", "1");
    }

    @ParameterRequest(key = "id", value = "1")
    void allowExactFilterIgnoreValue(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new ExactFilter("id").addIgnore("1"))
                .get();

        verify(hefesto, never()).where("id", "1");
    }

}
