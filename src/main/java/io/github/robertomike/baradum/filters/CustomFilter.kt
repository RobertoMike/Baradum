package io.github.robertomike.baradum.filters;

import io.github.robertomike.hefesto.builders.Hefesto;

public class CustomFilter extends Filter {

    private final CustomLambda custom;

    public CustomFilter(String field, CustomLambda custom) {
        super(field, field);
        this.custom = custom;
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        custom.apply(query, value);
    }

    public interface CustomLambda {
        void apply(Hefesto<?> builder, String value);
    }
}
