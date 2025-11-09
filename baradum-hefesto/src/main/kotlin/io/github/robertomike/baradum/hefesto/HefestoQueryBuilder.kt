package io.github.robertomike.baradum.hefesto

import io.github.robertomike.baradum.core.enums.BaradumOperator
import io.github.robertomike.baradum.core.enums.SortDirection
import io.github.robertomike.baradum.core.enums.WhereOperator
import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.models.Page
import io.github.robertomike.baradum.hefesto.converters.OperatorConverter
import io.github.robertomike.baradum.hefesto.converters.SortConverter
import io.github.robertomike.baradum.hefesto.converters.WhereOperatorConverter
import io.github.robertomike.hefesto.actions.Select
import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.constructors.ConstructWhereImplementation
import io.github.robertomike.hefesto.models.BaseModel
import java.util.Optional

/**
 * Hefesto implementation of QueryBuilder
 */
class HefestoQueryBuilder<T : BaseModel>(
    private val hefestoBuilder: Hefesto<T>
) : QueryBuilder<T> {

    constructor(modelClass: Class<T>) : this(Hefesto.make(modelClass))

    override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): QueryBuilder<T> {
        val whereClause = Where(
            field,
            OperatorConverter.toHefesto(operator),
            value,
            WhereOperatorConverter.toHefesto(whereOperator)
        )
        hefestoBuilder.where(whereClause)
        return this
    }

    override fun orderBy(field: String, direction: SortDirection): QueryBuilder<T> {
        hefestoBuilder.orderBy(field, SortConverter.toHefesto(direction))
        return this
    }

    override fun select(vararg fields: String): QueryBuilder<T> {
        hefestoBuilder.setSelects(*fields)
        return this
    }

    override fun addSelect(vararg fields: String): QueryBuilder<T> {
        fields.forEach { hefestoBuilder.addSelect(it) }
        return this
    }

    fun addSelect(vararg selects: Select): HefestoQueryBuilder<T> {
        hefestoBuilder.addSelect(*selects)
        return this
    }

    override fun limit(limit: Int): QueryBuilder<T> {
        hefestoBuilder.limit = limit
        return this
    }

    override fun offset(offset: Long): QueryBuilder<T> {
        hefestoBuilder.offset = offset.toInt()
        return this
    }

    override fun get(): List<T> {
        return hefestoBuilder.get()
    }

    override fun page(limit: Int, offset: Long): Page<T> {
        val hefestoPage = hefestoBuilder.page(limit, offset)
        return Page(
            content = hefestoPage.data,
            totalElements = hefestoPage.total,
            limit = limit,
            offset = offset
        )
    }

    override fun findFirst(): Optional<T> {
        return hefestoBuilder.findFirst()
    }

    override fun getWhereConditions(): ConstructWhereImplementation {
        return hefestoBuilder.wheres
    }

    /**
     * Get the underlying Hefesto builder for advanced operations
     */
    fun getHefestoBuilder(): Hefesto<T> {
        return hefestoBuilder
    }
}
