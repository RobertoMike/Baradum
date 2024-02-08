package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import static org.mockito.Mockito.verify;

@CustomTest
public class IntervalFilterTest {

    @ParameterRequest(key = "age", value = "18,65")
    void allowIntervalFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .get();

        verify(hefesto).where("age", Operator.GREATER_OR_EQUAL, "18");
        verify(hefesto).where("age", Operator.LESS_OR_EQUAL, "65");
    }

    @ParameterRequest(key = "age", value = "25")
    void allowIntervalFilterDifferentFormat(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .get();

        verify(hefesto).where("age", Operator.GREATER_OR_EQUAL, "25");
    }

    @ParameterRequest(key = "alias", value = "30,40")
    void allowIntervalFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("alias", "age"))
                .get();


        verify(hefesto).where("age", Operator.GREATER_OR_EQUAL, "30");
        verify(hefesto).where("age", Operator.LESS_OR_EQUAL, "40");
    }

    @ParameterRequest(key = "age", value = ",40")
    void allowIntervalFilterOnlyLessOrEqual(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new IntervalFilter("age"))
                .get();

        verify(hefesto).where("age", Operator.LESS_OR_EQUAL, "40");
    }
}
