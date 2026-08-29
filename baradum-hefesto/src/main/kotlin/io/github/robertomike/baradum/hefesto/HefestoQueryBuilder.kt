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
import io.github.robertomike.hefesto.actions.wheres.WhereCustom
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.constructors.ConstructWhereImplementation
import io.github.robertomike.hefesto.models.BaseModel
import jakarta.persistence.criteria.Expression
import java.util.Optional

/**
 * Hefesto implementation of QueryBuilder
 */
class HefestoQueryBuilder<T : BaseModel>(
    private val hefestoBuilder: Hefesto<T>
) : QueryBuilder<T> {

    constructor(modelClass: Class<T>) : this(Hefesto.make(modelClass))

    override fun where(field: String, operator: BaradumOperator, value: Any?, whereOperator: WhereOperator): QueryBuilder<T> {
        // Hefesto's own Operator enum has no BETWEEN and no case-insensitive LIKE, so these two
        // are built directly as raw Criteria API predicates instead of going through Where(...).
        when (operator) {
            BaradumOperator.BETWEEN -> applyBetween(field, value, whereOperator)
            BaradumOperator.LIKE_IGNORE_CASE -> applyLikeIgnoreCase(field, value, whereOperator)
            else -> {
                val whereClause = Where(
                    field,
                    OperatorConverter.toHefesto(operator),
                    value,
                    WhereOperatorConverter.toHefesto(whereOperator)
                )
                hefestoBuilder.where(whereClause)
            }
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyBetween(field: String, value: Any?, whereOperator: WhereOperator) {
        val (low, high) = when (value) {
            is List<*> -> value[0] to value[1]
            is Pair<*, *> -> value.first to value.second
            is Array<*> -> value[0] to value[1]
            else -> throw IllegalArgumentException("BETWEEN operator requires a pair of values")
        }

        val custom = WhereCustom.Custom { cb, _, root, _, _ ->
            cb.between(
                root.get<Comparable<Any>>(field) as Expression<Comparable<Any>>,
                low as Comparable<Any>,
                high as Comparable<Any>
            )
        }

        if (whereOperator == WhereOperator.OR) hefestoBuilder.orWhereCustom(custom) else hefestoBuilder.whereCustom(custom)
    }

    private fun applyLikeIgnoreCase(field: String, value: Any?, whereOperator: WhereOperator) {
        val pattern = (value as String).lowercase()

        val custom = WhereCustom.Custom { cb, _, root, _, _ ->
            cb.like(cb.lower(root.get<String>(field)), pattern)
        }

        if (whereOperator == WhereOperator.OR) hefestoBuilder.orWhereCustom(custom) else hefestoBuilder.whereCustom(custom)
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
