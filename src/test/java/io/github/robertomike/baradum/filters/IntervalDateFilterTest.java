package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@CustomTest
public class IntervalDateFilterTest {
    static final String FORMAT = "yyyy-MM-dd";

    @ParameterRequest(key = "date", value = "2022-01-01")
    void allowIntervalFilter(Hefesto<User> hefesto) throws ParseException {
        Baradum.make(User.class)
                .allowedFilters(new IntervalDateFilter("date"))
                .get();

        var format = new SimpleDateFormat(FORMAT);

        verify(hefesto).where(
                "date", Operator.GREATER_OR_EQUAL, format.parse("2022-01-01")
        );
    }

    @ParameterRequest(key = "date", value = ",2022-01-01")
    void allowIntervalFilterLessOrEqual(Hefesto<User> hefesto) throws ParseException {
        Baradum.make(User.class)
                .allowedFilters(new IntervalDateFilter("date"))
                .get();

        var format = new SimpleDateFormat(FORMAT);

        verify(hefesto).where(
                "date", Operator.LESS_OR_EQUAL, format.parse("2022-01-01")
        );
    }

    @ParameterRequest(key = "date", value = "2022-asd-01")
    void allowIntervalFilterError() {
        assertThrows(FilterException.class, () ->
                Baradum.make(User.class)
                        .allowedFilters(new IntervalDateFilter("date"))
                        .get()
        );
    }

    @ParameterRequest(key = "date", value = "01-01-2022")
    void allowIntervalFilterDifferentFormat(Hefesto<User> hefesto) throws ParseException {
        IntervalDateFilter.setFormat("dd-MM-yyyy");

        Baradum.make(User.class)
                .allowedFilters(new IntervalDateFilter("date"))
                .get();

        IntervalDateFilter.setFormat(FORMAT);

        var format = new SimpleDateFormat(FORMAT);

        verify(hefesto).where(
                "date", Operator.GREATER_OR_EQUAL, format.parse("2022-01-01")
        );
    }

    @ParameterRequest(key = "alias", value = "2022-01-01,2022-01-02")
    void allowIntervalFilterWithAlias(Hefesto<User> hefesto) throws ParseException {
        Baradum.make(User.class)
                .allowedFilters(new IntervalDateFilter("alias", "date"))
                .get();

        var format = new SimpleDateFormat(FORMAT);

        verify(hefesto).where(
                "date", Operator.GREATER_OR_EQUAL, format.parse("2022-01-01")
        );
        verify(hefesto).where(
                "date", Operator.LESS_OR_EQUAL, format.parse("2022-01-02")
        );
    }
}
