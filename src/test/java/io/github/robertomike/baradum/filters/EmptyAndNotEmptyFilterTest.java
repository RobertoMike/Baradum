package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
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
public class EmptyAndNotEmptyFilterTest {

    @ParameterRequest(key = "id", value = "")
    void allowEmptyFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EmptyFilter("id"))
                .get();

        verifyEmpty(hefesto);
    }

    private void verifyEmpty(Hefesto<User> hefesto) {
        ArgumentCaptor<BaseWhere[]> argument = ArgumentCaptor.forClass(BaseWhere[].class);

        verify(hefesto).where(argument.capture());

        assertNotNull(argument.getValue());

        var elements = List.of(argument.getValue());
        elements.forEach(baseWhere -> {
            var where = (Where) baseWhere;
            assertEquals("id", where.getField());
            assertTrue(where.getOperator() == Operator.IS_NULL || where.getOperator() == Operator.EQUAL);
        });

        assertTrue(elements.stream().anyMatch(baseWhere -> ((Where) baseWhere).getOperator() == Operator.IS_NULL));
        assertTrue(elements.stream().anyMatch(baseWhere -> ((Where) baseWhere).getOperator() == Operator.EQUAL));

        assertTrue(elements.stream().anyMatch(baseWhere -> baseWhere.getWhereOperation() == WhereOperator.OR));
        assertTrue(elements.stream().anyMatch(baseWhere -> baseWhere.getWhereOperation() == WhereOperator.AND));
    }

    @ParameterRequest(key = "alias", value = "")
    void allowEmptyFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EmptyFilter("alias", "id"))
                .get();

        verifyEmpty(hefesto);
    }

    @ParameterRequest(key = "id", value = "")
    void allowNotEmptyFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new NotEmptyFilter("id"))
                .get();

        verifyNotEmpty(hefesto);
    }

    private void verifyNotEmpty(Hefesto<User> hefesto) {
        ArgumentCaptor<BaseWhere[]> argument = ArgumentCaptor.forClass(BaseWhere[].class);

        verify(hefesto).where(argument.capture());

        assertNotNull(argument.getValue());

        var elements = List.of(argument.getValue());
        elements.forEach(baseWhere -> {
            var where = (Where) baseWhere;
            assertEquals("id", where.getField());
            assertEquals(WhereOperator.AND, where.getWhereOperation());
            assertTrue(where.getOperator() == Operator.IS_NOT_NULL || where.getOperator() == Operator.DIFF);
        });

        assertTrue(elements.stream().anyMatch(baseWhere -> ((Where) baseWhere).getOperator() == Operator.IS_NOT_NULL));
        assertTrue(elements.stream().anyMatch(baseWhere -> ((Where) baseWhere).getOperator() == Operator.DIFF));
    }

    @ParameterRequest(key = "alias", value = "")
    void allowNotEmptyFilterWithAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new NotEmptyFilter("alias", "id"))
                .get();

        verifyNotEmpty(hefesto);
    }

}
