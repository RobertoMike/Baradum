package io.github.robertomike.baradum

import io.github.robertomike.baradum.exceptions.BaradumException
import io.github.robertomike.baradum.filters.Filter
import io.github.robertomike.baradum.filters.Filterable
import io.github.robertomike.baradum.requests.BasicRequest
import io.github.robertomike.baradum.sorting.OrderBy
import io.github.robertomike.baradum.sorting.Sortable
import io.github.robertomike.hefesto.actions.Select
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.constructors.ConstructWhereImplementation
import io.github.robertomike.hefesto.models.BaseModel
import io.github.robertomike.hefesto.utils.ConditionalBuilder
import io.github.robertomike.hefesto.utils.Page
import java.util.Optional
import java.util.function.Consumer

class Baradum<T: BaseModel>(model: Class<T>): ConditionalBuilder<Baradum<T>> {
    private var builder: Hefesto<T> = Hefesto.make(model)
    private var sortable = Sortable()
    private var filterable = Filterable()
    private var useBody = false
    private var onlyBody = false

    companion object {
        /**
         * request for Baradum resolve params and body
         */
        @JvmStatic lateinit var request: BasicRequest<out Any>

        /**
         * Creates a new instance of Baradum with the specified model class.
         *
         * @param model the model class for Baradum
         * @return a new instance of Baradum with the specified model class
         */
        @JvmStatic
        fun <T: BaseModel> make(model: Class<T>): Baradum<T> {
            return Baradum(model)
        }
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     *
     * @param filters the filters to be added
     * @return the updated Baradum instance
     */
    fun allowedFilters(vararg filters: Filter<*>): Baradum<T> {
        filterable.addFilters(*filters)
        return this
    }

    /**
     * Adds the specified filters to the list of allowed filters.
     *
     * @param filters the filters to be added
     * @return the updated Baradum instance
     */
    fun allowedFilters(filters: List<Filter<*>>): Baradum<T> {
        filterable.addFilters(filters)
        return this
    }

    /**
     * Adds allowed filters to the Baradum object with ExactFilter.
     *
     * @param filters varargs of filters to be added
     * @return the Baradum object
     */
    fun allowedFilters(vararg filters: String): Baradum<T> {
        filterable.addFilters(*filters)
        return this
    }

    /**
     * Add allowed sort criteria to the Baradum object.
     *
     * @param sorts variable number of sort criteria
     * @return the updated Baradum object
     */
    fun allowedSort(vararg sorts: String): Baradum<T> {
        sortable.addSorts(*sorts)
        return this
    }

    /**
     * A method to add allowed sorts to the Baradum object.
     *
     * @param sorts an array of OrderBy objects representing the allowed sorts
     * @return the Baradum object with the added allowed sorts
     */
    fun allowedSort(vararg sorts: OrderBy): Baradum<T> {
        sortable.addSorts(*sorts)
        return this
    }

    /**
     * A method to add allowed sorts to the Baradum object.
     *
     * @param sorts an array of OrderBy objects representing the allowed sorts
     * @return the Baradum object with the added allowed sorts
     */
    fun allowedSort(sorts: List<OrderBy>): Baradum<T> {
        sortable.addSorts(sorts)
        return this
    }

    /**
     * Reset the list of select and set the passed selects
     *
     * @param selects the selects
     * @return the current instance
     */
    fun selects(vararg selects: String): Baradum<T> {
        builder.setSelects(*selects)
        return this
    }

    /**
     * Add new select to the current list
     *
     * @param selects the selects
     * @return the current instance
     */
    fun addSelects(vararg selects: String): Baradum<T> {
        listOf(*selects).forEach(builder::addSelect)
        return this
    }

    /**
     * Add new select to the current list
     *
     * @param selects the selects
     * @return the current instance
     */
    fun addSelects(vararg selects: Select): Baradum<T> {
        builder.addSelect(*selects)
        return this
    }

    /**
     * Sets the value of the `useBody` variable to `true` and returns the current instance of the class.
     * <br>
     * Using this method, allow you to use this class with POST requests and at the same time this request params for GET
     *
     * @return The current instance of the class.
     */
    fun useBody(): Baradum<T> {
        useBody = true
        return this
    }

    /**
     * Sets the value of the `useBody` variable to `true`, onlyBody to `true` and returns the current instance of the class.
     * <br>
     * Using this method, only allow you to use this class with POST requests. If used in other ways will throw an exception
     *
     * @return The current instance of the class.
     */
    fun useOnlyBody(): Baradum<T> {
        useBody = true
        onlyBody = true
        return this
    }

    /**
     * Apply the filters and sorts to Hefesto.
     * <br>
     * If the request is a POST and the `useBody` is true, the body will be read and the filters and sorts will be applied.
     * <br>
     * If the request is not a POST or the `useBody` is false, the filters and sorts will be applied from the parameters of the request.
     * <br>
     * If the `onlyBody` is true and the request is not a POST, an exception will be thrown.
     */
    private fun apply() {
        if (!request.isPost() && onlyBody) {
            throw BaradumException("Body can only be used with POST requests")
        }

        if (useBody && request.isPost()) {
            val body = request.getBody() ?: throw BaradumException("No body in request")

            filterable.apply(builder, body.filters)
            sortable.apply(builder, body.sorts)
            return
        }

        filterable.apply(builder, request)
        sortable.apply(builder, request)
    }

    /**
     * Get the list of type T.
     *
     * @return the list of type T
     */
    fun get(): List<T> {
        apply()
        return builder.get()
    }

    /**
     * A method to retrieve a page of elements.
     *
     * @param limit  the maximum number of items to retrieve
     * @param offset the starting position of the items to retrieve
     * @return a page of elements
     */
    fun page(limit: Int, offset: Long): Page<T> {
        apply()
        return builder.page(limit, offset)
    }

    /**
     * Returns a page of items with the specified limit.
     *
     * @param limit the maximum number of items to include in the page
     * @return a page of items
     */
    fun page(limit: Int): Page<T> {
        return page(limit, 0)
    }

    /**
     * Returns an optional with a single element.
     *
     * @return optionalWith a single element
     */
    fun findFirst(): Optional<T> {
        apply()
        return builder.findFirst()
    }

    override fun getWheres(): ConstructWhereImplementation {
        return builder.wheres
    }

    /**
     * Sets up the builder for creating a {@link Hefesto} object.
     *
     * @param consumer a consumer function that takes a {@link Hefesto} object as input and performs some operations on it.
     * @param <T>      the type parameter for the {@link Hefesto} object.
     */
    fun builder(lambda: Consumer<Hefesto<T>>): Baradum<T>  {
        lambda.accept(builder)
        return this
    }
}
