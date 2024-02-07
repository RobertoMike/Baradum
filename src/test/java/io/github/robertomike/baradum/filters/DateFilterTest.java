package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@CustomTest
public class DateFilterTest {
    public static Stream<Arguments> dateFilters() {
        return Stream.of(
                Arguments.of(
                        "01-01-2022", "01-01-2022", Operator.EQUAL
                ),
                Arguments.of(
                        "01-01-2022", "<=01-01-2022", Operator.LESS_OR_EQUAL
                ),
                Arguments.of(
                        "01-01-2022", "<01-01-2022", Operator.LESS
                ),
                Arguments.of(
                        "01-01-2022", ">=01-01-2022", Operator.GREATER_OR_EQUAL
                ),
                Arguments.of(
                        "01-01-2022", ">01-01-2022", Operator.GREATER
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dateFilters")
    void allowDateFilter(String date, String param, Operator operator, Hefesto<User> hefesto, BasicRequest<?> request) throws ParseException {
        when(request.findByName("date")).thenReturn(param);

        Baradum.make(User.class)
                .allowedFilters(new DateFilter("date"))
                .get();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        verify(hefesto).where(
                "date", operator, format.parse(date)
        );
    }

    @ParameterRequest(key = "date", value = "2022-asd-01")
    void allowIntervalFilterError() {
        assertThrows(FilterException.class, () ->
                Baradum.make(User.class)
                        .allowedFilters(new DateFilter("date"))
                        .get()
        );
    }

    @ParameterRequest(key = "alias", value = "2022-01-01")
    void allowIntervalFilterInternalName(Hefesto<User> hefesto) throws ParseException {
        Baradum.make(User.class)
                .allowedFilters(new DateFilter("alias", "date"))
                .get();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        verify(hefesto).where(
                "date", Operator.EQUAL, format.parse("2022-01-01")
        );
    }

    @ParameterRequest(key = "alias", value = "01-01-2022")
    void allowIntervalFilterChangeFormat(Hefesto<User> hefesto) throws ParseException {
        DateFilter.setFormat("dd-MM-yyyy");

        Baradum.make(User.class)
                .allowedFilters(new DateFilter("alias", "date"))
                .get();

        DateFilter.setFormat("yyyy-MM-dd");

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        verify(hefesto).where(
                "date", Operator.EQUAL, format.parse("01-01-2022")
        );
    }
}
