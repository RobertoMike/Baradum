package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.BodyRequest;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.filters.DateFilter;
import io.github.robertomike.baradum.filters.EmptyFilter;
import io.github.robertomike.baradum.filters.EnumFilter;
import io.github.robertomike.baradum.filters.ExactFilter;
import io.github.robertomike.baradum.models.Status;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.actions.wheres.BaseWhere;
import io.github.robertomike.hefesto.actions.wheres.CollectionWhere;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;
import org.mockito.ArgumentCaptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@CustomTest
public class FilterableByBodyTest {
    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"EQUAL\",\"value\":\"1\"}]}")
    void allowExactFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id")
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals("1", where.getValue());
        assertEquals(Operator.EQUAL, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"EQUAL\",\"value\":\"1\"}]}")
    void ignoreValue(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new ExactFilter("id").addIgnore("1"))
                .useBody()
                .get();

        verify(hefesto, never()).where(any(BaseWhere.class));
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"EQUAL\",\"value\":\"ACTIVE\"}]}")
    void allowEnumFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("id", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals(Status.ACTIVE, where.getValue());
        assertEquals(Operator.EQUAL, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"IS_NOT_NULL\"}]}")
    void allowNotNull(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("id", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertNull(where.getValue());
        assertEquals(Operator.IS_NOT_NULL, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"IS_NULL\"}]}")
    void allowNull(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("id", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertNull(where.getValue());
        assertEquals(Operator.IS_NULL, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"IN\",\"value\":\"ACTIVE,INACTIVE\"}]}")
    void allowEnumFilterWithInOperator(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("id", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals(List.of(Status.ACTIVE, Status.INACTIVE), where.getValue());
        assertEquals(Operator.IN, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"NOT_IN\",\"value\":\"ACTIVE,INACTIVE\"}]}")
    void allowEnumFilterWithNotInOperator(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(new EnumFilter<>("id", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals(List.of(Status.ACTIVE, Status.INACTIVE), where.getValue());
        assertEquals(Operator.NOT_IN, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"EQUAL\",\"value\":\"01-01-2022\"}]}")
    void allowDateFilter(Hefesto<User> hefesto) throws ParseException {
        Baradum.make(User.class)
                .allowedFilters(new DateFilter("id"))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("01-01-2022"), where.getValue());
        assertEquals(Operator.EQUAL, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"operator\":\"LIKE\",\"value\":\"1%\"}]}")
    void allowLikeFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id")
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var where = (Where) argument.getValue();

        assertEquals("id", where.getField());
        assertEquals("1%", where.getValue());
        assertEquals(Operator.LIKE, where.getOperator());
        assertEquals(WhereOperator.AND, where.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"},{\"field\":\"name\",\"value\":\"abc%\",\"operator\":\"LIKE\"}]}")
    void allowMoreThatOneFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id", "name")
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto, times(2)).where(argument.capture());

        var wheres = argument.getAllValues();
        var whereEqual = (Where) wheres.get(0);
        var wherePartial = (Where) wheres.get(1);

        assertEquals("id", whereEqual.getField());
        assertEquals("1", whereEqual.getValue());
        assertEquals(Operator.EQUAL, whereEqual.getOperator());
        assertEquals(WhereOperator.AND, whereEqual.getWhereOperation());

        assertEquals("name", wherePartial.getField());
        assertEquals("abc%", wherePartial.getValue());
        assertEquals(Operator.LIKE, wherePartial.getOperator());
        assertEquals(WhereOperator.AND, wherePartial.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"subFilters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"},{\"field\":\"status\",\"value\":\"ACTIVE\",\"operator\":\"EQUAL\",\"type\":\"OR\"}]}]}")
    void allowSubFilters(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters("id")
                .allowedFilters(new EnumFilter<>("status", Status.class))
                .useBody()
                .get();

        ArgumentCaptor<BaseWhere> argument = ArgumentCaptor.forClass(BaseWhere.class);

        verify(hefesto).where(argument.capture());

        var wheres = (CollectionWhere) argument.getValue();
        var whereEqual = (Where) wheres.getWheres().get(0);
        var whereEnum = (Where) wheres.getWheres().get(1);

        assertEquals("id", whereEqual.getField());
        assertEquals("1", whereEqual.getValue());
        assertEquals(Operator.EQUAL, whereEqual.getOperator());
        assertEquals(WhereOperator.AND, whereEqual.getWhereOperation());

        assertEquals("status", whereEnum.getField());
        assertEquals(Status.ACTIVE, whereEnum.getValue());
        assertEquals(Operator.EQUAL, whereEnum.getOperator());
        assertEquals(WhereOperator.OR, whereEnum.getWhereOperation());
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"},{\"field\":\"name\",\"value\":\"abc%\",\"operator\":\"LIKE\"}]}")
    void notAllowedFilter() {
        assertThrows(FilterException.class, () ->
                Baradum.make(User.class)
                        .allowedFilters("id")
                        .useBody()
                        .get()
        );
    }

    @BodyRequest("{\"filters\":[{\"field\":\"id\",\"value\":\"1\",\"operator\":\"EQUAL\"}]}")
    void unsupportedFilter() {
        assertThrows(FilterException.class, () ->
                Baradum.make(User.class)
                        .allowedFilters(new EmptyFilter("id"))
                        .useBody()
                        .get()
        );
    }
}
