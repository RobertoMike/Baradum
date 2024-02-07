package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import static org.mockito.Mockito.verify;

@CustomTest
public class PartialFilterTest {
    @ParameterRequest(key = "id", value = "1")
    void allowPartialFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new PartialFilter("id"))
                .get();

        verify(hefesto).where("id", Operator.LIKE, "1%");
    }

    @ParameterRequest(key = "alias", value = "1")
    void allowPartialFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new PartialFilter("alias", "id"))
                .get();

        verify(hefesto).where("id", Operator.LIKE, "1%");
    }
}
