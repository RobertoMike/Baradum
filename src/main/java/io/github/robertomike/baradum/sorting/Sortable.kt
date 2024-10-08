package io.github.robertomike.baradum.sorting;

import io.github.robertomike.baradum.exceptions.SortableException;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.baradum.requests.OrderRequest;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Sortable {
    public List<OrderBy> allowedSorts = new ArrayList<>();

    public void addSorts(String... sorts) {
        Arrays.asList(sorts).forEach(sort -> allowedSorts.add(new OrderBy(sort)));
    }

    public void addSorts(OrderBy... sorts) {
        allowedSorts.addAll(List.of(sorts));
    }

    public void addSorts(List<OrderBy> sorts) {
        allowedSorts.addAll(sorts);
    }

    public void apply(Hefesto<?> builder, BasicRequest<?> request) {
        if (request.notExistsByName("sort")) {
            return;
        }

        String[] sorts = request.findByName("sort")
                .trim().split(",");

        var sortList = Arrays.asList(sorts).stream().map(sort -> new OrderRequest(
                sort.replace("-", ""),
                sort.contains("-") ? Sort.DESC : Sort.ASC
        )).toList();

        apply(builder, sortList);
    }

    public void apply(Hefesto<?> builder, List<OrderRequest> sorts) {
        sorts.forEach(sort -> {
            Optional<OrderBy> optionalOrderBy = allowedSorts
                    .stream()
                    .filter(allowedSort -> allowedSort.name().equals(sort.getField()))
                    .findFirst();

            optionalOrderBy.ifPresentOrElse(
                    orderBy -> builder.orderBy(orderBy.internalName(), sort.getSort()),
                    () -> {
                        throw new SortableException("The field '" + sort.getField() + "' is not valid");
                    }
            );
        });
    }
}
