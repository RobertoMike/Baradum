package io.github.robertomike.baradum.filters;

import io.github.robertomike.hefesto.builders.Hefesto;

public class ExactFilter extends Filter {
    public ExactFilter(String field, String internalName) {
        super(field, internalName);
    }

    public ExactFilter(String field) {
        super(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        query.where(internalName, value);
    }

    @Override
    public boolean supportBodyOperation() {
        return true;
    }
}
