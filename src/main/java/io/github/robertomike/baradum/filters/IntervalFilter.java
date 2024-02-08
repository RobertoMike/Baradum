package io.github.robertomike.baradum.filters;

import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

public class IntervalFilter extends Filter {
    public IntervalFilter(String field, String internalName) {
        super(field, internalName);
    }

    public IntervalFilter(String field) {
        super(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        String start;
        String end = null;
        if (value.contains(",")) {
            String[] values = value.split(",");
            start = values[0];
            end = values[1];
        } else {
            start = value;
        }

        if (end != null) {
            query.where(
                    internalName,
                    Operator.LESS_OR_EQUAL,
                    end
            );
        }

        if (!start.isBlank()) {
            query.where(
                    internalName,
                    Operator.GREATER_OR_EQUAL,
                    start
            );
        }
    }
}
