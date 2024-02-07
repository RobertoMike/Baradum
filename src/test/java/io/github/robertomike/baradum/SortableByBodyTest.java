package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.BodyRequest;
import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.exceptions.SortableException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.baradum.sorting.OrderBy;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Sort;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@CustomTest
public class SortableByBodyTest {
    @BodyRequest("{\"sorts\":[{\"field\":\"id\"},{\"field\":\"name\",\"sort\":\"DESC\"}]}")
    void sortByBody(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedSort("id", "name")
                .useBody()
                .get();

        verify(hefesto).orderBy("id", Sort.ASC);
        verify(hefesto).orderBy("name", Sort.DESC);
    }

    @BodyRequest("{\"sorts\":[{\"field\":\"id\"}]}")
    void sortByBodyByAlias(Hefesto<User> hefesto) {
        Baradum.make(User.class)
                .allowedSort(new OrderBy("id", "name"))
                .useBody()
                .get();

        verify(hefesto).orderBy("name", Sort.ASC);
    }

    @BodyRequest("{\"sorts\":[{\"field\":\"id\"}]}")
    void unsupportedSort() {
        assertThrows(SortableException.class, () ->
                Baradum.make(User.class)
                        .allowedSort("name")
                        .useBody()
                        .get()
        );
    }
}
