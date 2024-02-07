package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.models.Status;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.actions.wheres.BaseWhere;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@CustomTest
public class SetFilterTest {
    @ParameterRequest(key = "id", value = "ACTIVE")
    void allowedSetFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new SetFilter<>("id", Status.class))
                .get();

        verify(hefesto).where(
                "id",
                Operator.FIND_IN_SET,
                Status.ACTIVE
        );
    }

    @ParameterRequest(key = "id", value = "ABAB")
    void allowedSetFilterError() {
        assertThrows(FilterException.class, () ->
                Baradum.make(User.class)
                        .allowedFilters(new SetFilter<>("id", Status.class))
                        .get()
        );
    }

    @ParameterRequest(key = "id", value = "ACTIVE")
    void allowedSetFilterUsingNotFindInSet(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new SetFilter<>("id", Status.class, false))
                .get();

        verify(hefesto).where(
                "id",
                Operator.NOT_FIND_IN_SET,
                Status.ACTIVE
        );
    }

    @ParameterRequest(key = "id", value = "ACTIVE,INACTIVE")
    void allowedSetFilterWithMultipleValues(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new SetFilter<>("id", Status.class))
                .get();

        ArgumentCaptor<List<BaseWhere>> argument = ArgumentCaptor.forClass(List.class);

        verify(hefesto).where(argument.capture());

        assertNotNull(argument.getValue());

        argument.getValue().forEach(baseWhere -> {
            var where = (Where) baseWhere;
            assertEquals("id", where.getField());
            assertEquals(Operator.FIND_IN_SET, where.getOperator());
            assertEquals(WhereOperator.AND, where.getWhereOperation());
        });
    }

    @ParameterRequest(key = "id", value = "ACTIVE|INACTIVE")
    void allowedSetFilterWithMultipleValuesUsingOr(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new SetFilter<>("id", Status.class))
                .get();

        ArgumentCaptor<List<BaseWhere>> argument = ArgumentCaptor.forClass(List.class);

        verify(hefesto).where(argument.capture());

        assertNotNull(argument.getValue());

        argument.getValue().forEach(baseWhere -> {
            var where = (Where) baseWhere;
            assertEquals("id", where.getField());
            assertEquals(Operator.FIND_IN_SET, where.getOperator());
            assertEquals(WhereOperator.OR, where.getWhereOperation());
        });
    }

    @ParameterRequest(key = "alias", value = "ACTIVE")
    void allowedSetFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new SetFilter<>("alias", "id", Status.class))
                .get();

        verify(hefesto).where(
                "id",
                Operator.FIND_IN_SET,
                Status.ACTIVE
        );
    }

}
