package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.exceptions.FilterException;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.enums.Operator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IntervalDateFilter extends Filter {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void setFormat(String format) {
        IntervalDateFilter.format = new SimpleDateFormat(format);
    }

    public IntervalDateFilter(String field, String internalName) {
        super(field, internalName);
    }

    public IntervalDateFilter(String field) {
        super(field, field);
    }

    @Override
    public void filterByParam(Hefesto<?> query, String value) {
        Date startDate;
        Date endDate = null;
        if (value.contains(",")) {
            String[] dates = value.split(",");
            startDate = transform(dates[0]);
            endDate = transform(dates[1]);
        } else {
            startDate = transform(value);
        }

        if (endDate != null) {
            query.where(
                    internalName,
                    Operator.LESS_OR_EQUAL,
                    endDate
            );
        }

        query.where(
                internalName,
                Operator.GREATER_OR_EQUAL,
                startDate
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
}
