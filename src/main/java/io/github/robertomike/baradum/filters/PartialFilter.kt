package io.github.robertomike.baradum.filters;

import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

public class PartialFilter extends Filter {

    public PartialFilter(String field, String internalName) {
        super(field, internalName);
    }

    public PartialFilter(String field) {
        super(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        query.where(
                internalName,
                Operator.LIKE,
                value + "%"
        );
    }
}
