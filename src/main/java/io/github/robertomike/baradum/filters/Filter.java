package io.github.robertomike.baradum.filters;

import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.hefesto.builders.Hefesto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public abstract class Filter {
    protected String field;
    protected String defaultValue = null;
    protected List<String> ignored = new ArrayList<>();
    protected String internalName;

    public Filter(String field, String internalName) {
        this.field = field;
        this.internalName = internalName;
    }

    public Filter addIgnore(String... ignored) {
        this.ignored = List.of(ignored);
        return this;
    }

    public Filter setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    protected boolean ignore(String value) {
        var val = value.trim();

        return ignored.stream().anyMatch(ignore -> Objects.equals(ignore, val));
    }

    public abstract void filterByParam(Hefesto<?> query, String value);

    public void filterByParam(Hefesto<?> query, BasicRequest<?> request) {
        if (request.notExistsByName(field) && defaultValue == null) {
            return;
        }

        var parameter = request.findByName(field);

        if (parameter == null) {
            parameter = defaultValue;
        }

        if (parameter == null) {
            return;
        }

        if (ignore(parameter)) {
            return;
        }

        filterByParam(query, parameter);
    }

    public <T> T transform(String value) {
        return (T) value;
    }

    public boolean supportBodyOperation() {
        return false;
    }
}
