package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumFilter<T extends Enum<T>> extends Filter {
    private final Class<T> classEnum;

    public EnumFilter(String field, String internalName, Class<T> classEnum) {
        super(field, internalName);
        this.classEnum = classEnum;
    }

    public EnumFilter(String field, Class<T> classEnum) {
        super(field, field);
        this.classEnum = classEnum;
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        if (value.contains(",")) {
            Set<T> values = Arrays.stream(value.split(","))
                    .map(this::transform)
                    .collect(Collectors.toSet());

            query.where(internalName, Operator.IN, values);
            return;
        }

        query.where(internalName, transform(value));
    }

    public T transform(String value) {
        try {
            return Enum.valueOf(classEnum, value);
        } catch (NullPointerException | IllegalArgumentException e) {
            var allowed = String.join(", ", Arrays.stream(classEnum.getEnumConstants())
                    .map(Enum::name)
                    .toArray(String[]::new));
            throw new FilterException("Invalid value for " + field + ", allowed values: " + allowed);
        }
    }

    @Override
    public boolean supportBodyOperation() {
        return true;
    }
}
