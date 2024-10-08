package io.github.robertomike.baradum.requests;

import io.github.robertomike.hefesto.enums.Operator;
import io.github.robertomike.hefesto.enums.WhereOperator;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterRequest {
    private String field;
    private String value;
    private Operator operator = Operator.EQUAL;
    private WhereOperator type = WhereOperator.AND;
    private List<FilterRequest> subFilters = new ArrayList<>();
}
