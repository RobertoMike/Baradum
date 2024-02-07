package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;

import static org.mockito.Mockito.verify;

@CustomTest
public class CustomFilterTest {
    @ParameterRequest(key = "id", value = "1")
    void allowCustomFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new CustomFilter("id", (builder, value) -> builder.where("name", value)))
                .get();

        verify(hefesto).where("name", "1");
    }
}
