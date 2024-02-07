package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFilter extends Filter {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void setFormat(String format) {
        DateFilter.format = new SimpleDateFormat(format);
    }

    public DateFilter(String field, String internalName) {
        super(field, internalName);
    }

    public DateFilter(String field) {
        this(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        Operator operator = Operator.EQUAL;

        if (value.contains("<=")) {
            operator = Operator.LESS_OR_EQUAL;
            value = value.replace("<=", "");
        } else if (value.contains(">=")) {
            operator = Operator.GREATER_OR_EQUAL;
            value = value.replace(">=", "");
        } else if (value.contains(">")) {
            operator = Operator.GREATER;
            value = value.replace(">", "");
        } else if (value.contains("<")) {
            operator = Operator.LESS;
            value = value.replace("<", "");
        }

        Date date = transform(value);

        query.where(
                internalName,
                operator,
                date
        );
    }

    @Override
    public <T> T transform(String value) {
        try {
            return (T) format.parse(value);
        } catch (Exception e) {
            throw new FilterException("invalid dates");
        }
    }

    @Override
    public boolean supportBodyOperation() {
        return true;
    }
}
