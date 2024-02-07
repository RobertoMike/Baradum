package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

public class NotEmptyFilter extends Filter {
    public NotEmptyFilter(String field, String internalName) {
        super(field, internalName);
    }

    public NotEmptyFilter(String field) {
        super(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, BasicRequest<?> request) {
        if (request.findByName(field) != null) {
            filterByParam(query, "");
        }
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        query.where(
                new Where(internalName, Operator.IS_NOT_NULL, null),
                new Where(internalName, Operator.DIFF, "")
        );
    }
}
