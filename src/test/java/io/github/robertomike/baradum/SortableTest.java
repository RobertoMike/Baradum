package io.github.robertomike.baradum;

import io.github.robertomike.baradum.configs.CustomTest;
import io.github.robertomike.baradum.configs.ParameterRequest;
import io.github.robertomike.baradum.exceptions.SortableException;
import io.github.robertomike.baradum.models.User;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.baradum.sorting.OrderBy;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Sort;
import io.github.robertomike.hefesto.utils.Page;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@CustomTest
public class SortableTest {

    @ParameterRequest(key = "sort", value = "id")
    void allowSort(Hefesto<User> hefesto, BasicRequest<?> request) {
        when(request.notExistsByName("sort")).thenReturn(false);
        when(hefesto.page(5, 1)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 1, 10));

        var users = Baradum.make(User.class)
                .allowedSort("id")
                .page(5, 1);

        verify(hefesto).orderBy("id", Sort.ASC);
        assertNotNull(users);
    }

    @ParameterRequest(key = "sort", value = "-id")
    void allowSortDesc(Hefesto<User> hefesto, BasicRequest<?> request) {
        when(request.notExistsByName("sort")).thenReturn(false);
        when(hefesto.page(5, 1)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 1, 10));

        var users = Baradum.make(User.class)
                .allowedSort("id")
                .page(5, 1);

        verify(hefesto).orderBy("id", Sort.DESC);
        assertNotNull(users);
    }

    @ParameterRequest(key = "sort", value = "name")
    void notAllowedSort(Hefesto<User> hefesto, BasicRequest<?> request) {
        when(request.notExistsByName("sort")).thenReturn(false);
        when(hefesto.page(5, 1)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 1, 10));

        assertThrows(SortableException.class, () ->
                Baradum.make(User.class)
                        .allowedSort("id")
                        .page(5, 1)
        );
    }

    @ParameterRequest(key = "sort", value = "-id")
    void sortByInternalName(Hefesto<User> hefesto, BasicRequest<?> request) {
        when(request.notExistsByName("sort")).thenReturn(false);
        when(hefesto.page(5, 1)).thenReturn(new Page<>(Collections.nCopies(5, mock(User.class)), 1, 10));

        var users = Baradum.make(User.class)
                .allowedSort(new OrderBy("id", "name"))
                .page(5, 1);

        verify(hefesto).orderBy("name", Sort.DESC);
        assertNotNull(users);
    }

    @ParameterRequest(key = "sort", value = "-id")
    void sortByUsingObject(Hefesto<User> hefesto, BasicRequest<?> request) {
        when(request.notExistsByName("sort")).thenReturn(false);

        Baradum.make(User.class)
                .allowedSort(new OrderBy("id"))
                .get();

        verify(hefesto).orderBy("id", Sort.DESC);
    }
}
