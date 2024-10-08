package io.github.robertomike.baradum;

import io.github.robertomike.baradum.exceptions.BaradumException;
import io.github.robertomike.baradum.filters.Filter;
import io.github.robertomike.baradum.filters.Filterable;
import io.github.robertomike.baradum.requests.BasicRequest;
import io.github.robertomike.baradum.sorting.OrderBy;
import io.github.robertomike.baradum.sorting.Sortable;
import io.github.robertomike.hefesto.actions.Select;
import io.github.robertomike.hefesto.builders.Hefesto;
import io.github.robertomike.hefesto.models.BaseModel;
import io.github.robertomike.hefesto.utils.Page;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Baradum<T extends BaseModel> {
    @Setter
    private static BasicRequest<?> request;
    private final Hefesto<T> builder;
    private final Sortable sortable = new Sortable();
    private final Filterable filterable = new Filterable();
    private boolean useBody = false;

    public Baradum(Class<T> model) {
        this.builder = Hefesto.make(model);
    }

    /**
     * Creates a new instance of Baradum with the specified model class.
     *
     * @param model the model class for Baradum
     * @return a new instance of Baradum with the specified model class
     */
    public static <T extends BaseModel> Baradum<T> make(Class<T> model) {
        return new Baradum<>(model);
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     *
     * @param filters the filters to be added
     * @return the updated Baradum instance
     */
    public Baradum<T> allowedFilters(Filter... filters) {
        filterable.addFilters(filters);
        return this;
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     *
     * @param filters the filters to be added
     * @return the updated Baradum instance
     */
    public Baradum<T> allowedFilters(List<Filter> filters) {
        filterable.addFilters(filters);
        return this;
    }

    /**
     * Adds allowed filters to the Baradum object with ExactFilter.
     *
     * @param filters varargs of filters to be added
     * @return the Baradum object
     */
    public Baradum<T> allowedFilters(String... filters) {
        filterable.addFilters(filters);
        return this;
    }

    /**
     * Add allowed sort criteria to the Baradum object.
     *
     * @param sorts variable number of sort criteria
     * @return the updated Baradum object
     */
    public Baradum<T> allowedSort(String... sorts) {
        sortable.addSorts(sorts);
        return this;
    }

    /**
     * A method to add allowed sorts to the Baradum object.
     *
     * @param sorts an array of OrderBy objects representing the allowed sorts
     * @return the Baradum object with the added allowed sorts
     */
    public Baradum<T> allowedSort(OrderBy... sorts) {
        sortable.addSorts(sorts);
        return this;
    }

    /**
     * A method to add allowed sorts to the Baradum object.
     *
     * @param sorts an array of OrderBy objects representing the allowed sorts
     * @return the Baradum object with the added allowed sorts
     */
    public Baradum<T> allowedSort(List<OrderBy> sorts) {
        sortable.addSorts(sorts);
        return this;
    }

    /**
     * Reset the list of select and set the passed selects
     *
     * @param selects the selects
     * @return the current instance
     */
    public Baradum<T> selects(String... selects) {
        builder.select(selects);
        return this;
    }

    /**
     * Add new select to the current list
     *
     * @param selects the selects
     * @return the current instance
     */
    public Baradum<T> addSelects(String... selects) {
        List.of(selects).forEach(builder::addSelect);
        return this;
    }

    /**
     * Add new select to the current list
     *
     * @param selects the selects
     * @return the current instance
     */
    public Baradum<T> addSelects(Select... selects) {
        builder.addSelect(selects);
        return this;
    }

    public Baradum<T> useBody() {
        useBody = true;
        return this;
    }

    private void apply() {
        if (!request.getMethod().equalsIgnoreCase("POST") && useBody) {
            throw new BaradumException("Body can only be used with POST requests");
        }

        if (useBody) {
            request.loadBody();
            var body = request.getBodyRequest();
            filterable.apply(builder, body.getFilters());
            sortable.apply(builder, body.getSorts());

            // Clean body
            request.cleanBody();
            return;

        }

        filterable.apply(builder, request);
        sortable.apply(builder, request);
    }

    /**
     * Get the list of type T.
     *
     * @return the list of type T
     */
    public List<T> get() {
        apply();
        return builder.get();
    }

    /**
     * A method to retrieve a page of elements.
     *
     * @param limit  the maximum number of items to retrieve
     * @param offset the starting position of the items to retrieve
     * @return a page of elements
     */
    public Page<T> page(int limit, long offset) {
        apply();
        return builder.page(limit, offset);
    }

    /**
     * Returns a page of items with the specified limit.
     *
     * @param limit the maximum number of items to include in the page
     * @return a page of items
     */
    public Page<T> page(int limit) {
        return page(limit, 0);
    }
}
