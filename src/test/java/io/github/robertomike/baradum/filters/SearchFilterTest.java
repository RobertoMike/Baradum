package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.Baradum;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@CustomTest
public class SearchFilterTest {
    @ParameterRequest(key = "search", value = "Ciao")
    void allowedSearchFilter(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedFilters(SearchFilter.of("name", "lastname", "email"))
                .get();

        verify(hefesto).where(Arrays.asList(
                new Where("name", Operator.LIKE, "Ciao%", WhereOperator.OR),
                new Where("lastname", Operator.LIKE, "Ciao%", WhereOperator.OR),
                new Where("email", Operator.LIKE, "Ciao%", WhereOperator.OR)
        ));
    }
}
