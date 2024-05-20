package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.baradum.requests.FilterRequest;
import io.github.robertomike.hefesto.actions.wheres.BaseWhere;
import io.github.robertomike.hefesto.actions.wheres.CollectionWhere;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Filterable {
    public List<Filter> allowedFilters = new ArrayList<>();

    public void addFilters(String... filters) {
        Arrays.asList(filters).forEach(sort -> allowedFilters.add(new ExactFilter(sort)));
    }

    public void addFilters(Filter... filters) {
        allowedFilters.addAll(List.of(filters));
    }

    public void addFilters(List<Filter> filters) {
        allowedFilters.addAll(filters);
    }

    public void apply(Hefesto<?> builder, BasicRequest<?> request) {
        allowedFilters.forEach((filter) -> filter.filterByParam(builder, request));
    }

    public void apply(Hefesto<?> builder, List<FilterRequest> filters) {
        filters.forEach(filterRequest -> {
                    var where = apply(filterRequest);
                    if (where == null) {
                        return;
                    }

                    builder.where(where);
                });
    }

    public BaseWhere apply(FilterRequest filter) {
        if (filter.getSubFilters().isEmpty() && filter.getField() == null) {
            throw new FilterException("The field and subFilters cannot be empty at the same time");
        }

        if (!filter.getSubFilters().isEmpty()) {
            return new CollectionWhere(
                    filter.getSubFilters().stream()
                            .map(this::apply)
                            .filter(Objects::nonNull)
                            .toList(),
                    filter.getType()
            );
        }

        return searchFilterAndExecute(filter);
    }

    private BaseWhere searchFilterAndExecute(FilterRequest filterRequest) {
        var filter = allowedFilters
                .stream()
                .filter(allowed -> allowed.getField().equals(filterRequest.getField()))
                .findFirst()
                .orElseThrow(() -> new FilterException("The field '" + filterRequest.getField() + "' is not allowed"));

        if (!filter.supportBodyOperation()) {
            throw new FilterException("The filter '" + filterRequest.getClass().getSimpleName() + "' not support body request");
        }

        var operator = filterRequest.getOperator();

        if (canApplyIgnore(operator) && filter.ignore(filterRequest.getValue())) {
            return null;
        }

        var field = filter.getInternalName();
        Object value = filterRequest.getValue();
        var whereOperator = filterRequest.getType();

        switch (operator) {
            case IN, NOT_IN -> value = Stream.of(value.toString().split(","))
                    .map(filter::transform)
                    .toList();
            case IS_NULL, IS_NOT_NULL -> value = null;
            default -> value = filter.transform((String) value);
        }

        return new Where(field,  operator, value, whereOperator);
    }

    private boolean canApplyIgnore(Operator operator) {
        return !(operator.equals(Operator.IS_NULL) || operator.equals(Operator.IS_NOT_NULL));
    }
}
