package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.models.Pet;
import io.github.robertomike.baradum.models.Status;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@CustomTest
public class EnumFilterTest {

    @ParameterRequest(key = "status", value = "ACTIVE")
    void allowEnumFilter(Hefesto<User> hefesto) {
        Baradum.make(Pet.class)
                .allowedFilters(new EnumFilter<>("status", Status.class))
                .get();

        verify(hefesto).where("status", Status.ACTIVE);
    }

    @ParameterRequest(key = "status", value = "ACTIVE,INACTIVE")
    void allowEnumFilterMoreThatOneValue(Hefesto<User> hefesto) {
        Baradum.make(Pet.class)
                .allowedFilters(new EnumFilter<>("status", Status.class))
                .get();

        verify(hefesto).where("status", Operator.IN, new HashSet<>(Arrays.asList(Status.ACTIVE, Status.INACTIVE)));
    }

    @ParameterRequest(key = "random_name", value = "ACTIVE")
    void allowEnumFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(Pet.class)
                .allowedFilters(new EnumFilter<>("random_name", "status", Status.class))
                .get();

        verify(hefesto).where("status", Status.ACTIVE);
    }

    @ParameterRequest(key = "status", value = "こんにちは")
    void allowEnumFilterInvalidValue() {
        assertThrows(FilterException.class, () ->
                Baradum.make(Pet.class)
                        .allowedFilters(new EnumFilter<>("status", Status.class))
                        .get()
        );
    }
}
