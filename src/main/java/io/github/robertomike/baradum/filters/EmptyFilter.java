package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;

public class EmptyFilter extends Filter {
    public EmptyFilter(String field, String internalName) {
        super(field, internalName);
    }

    public EmptyFilter(String field) {
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
                new Where(internalName, Operator.IS_NULL, null),
                Where.make(internalName, "", WhereOperator.OR)
        );
    }
}
