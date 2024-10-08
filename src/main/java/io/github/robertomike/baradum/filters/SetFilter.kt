package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.hefesto.actions.wheres.BaseWhere;
import io.github.robertomike.hefesto.actions.wheres.Where;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SetFilter<T extends Enum<T>> extends Filter {
    private final Class<T> classEnum;
    private final boolean not;

    public SetFilter(String field, String internalName, Class<T> classEnum) {
        this(field, internalName, classEnum, true);
    }
    public SetFilter(String field, String internalName, Class<T> classEnum, boolean not) {
        super(field, internalName);
        this.classEnum = classEnum;
        this.not = not;
    }
    public SetFilter(String field, Class<T> classEnum, boolean not) {
        this(field, field, classEnum, not);
    }
    public SetFilter(String field, Class<T> classEnum) {
        this(field, field, classEnum, true);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        var whereOperator = WhereOperator.AND;
        var operator = not ? Operator.FIND_IN_SET : Operator.NOT_FIND_IN_SET;
        var split = ",";

        if (value.contains("|")) {
            split = "\\|";
            whereOperator = WhereOperator.OR;
        }

        if (value.contains(",") || value.contains("|")) {
            Set<T> values = Arrays.stream(value.split(split))
                    .map(this::transform)
                    .collect(Collectors.toSet());
            List<BaseWhere> wheres = new ArrayList<>();

            for (var enumValue: values) {
                wheres.add(
                        new Where(
                                internalName,
                                operator,
                                enumValue,
                                whereOperator
                        )
                );
            }

            query.where(wheres);
            return;
        }

        query.where(
                internalName,
                operator,
                transform(value)
        );
    }

    public T transform(String value) {
        try {
            return Enum.valueOf(classEnum, value);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new FilterException("invalid value");
        }
    }
}
