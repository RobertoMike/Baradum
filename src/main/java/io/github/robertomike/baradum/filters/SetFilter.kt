package io.github.robertomike.baradum.filters

import io.github.robertomike.baradum.exceptions.FilterException
import io.github.robertomike.baradum.utils.valueOf
import io.github.robertomike.hefesto.actions.wheres.Where
import io.github.robertomike.hefesto.builders.Hefesto
import io.github.robertomike.hefesto.enums.Operator
import io.github.robertomike.hefesto.enums.WhereOperator

class SetFilter<T : Enum<T>>
@JvmOverloads constructor(
    field: String,
    internalName: String = field,
    private val classEnum: Class<T>,
    private val not: Boolean = true
) : Filter<T>(field, internalName) {

    constructor(field: String, classEnum: Class<T>, not: Boolean = true) : this(
        field,
        field,
        classEnum,
        not
    )

    override fun filterByParam(query: Hefesto<*>, value: String) {
        var whereOperator = WhereOperator.AND
        val operator = if (not) Operator.FIND_IN_SET else Operator.NOT_FIND_IN_SET
        var split = ","

        if (value.contains("|")) {
            split = "|"
            whereOperator = WhereOperator.OR
        }

        if (value.contains(",") || value.contains("|")) {
            val values = value.split(split)
                .map(this::transform)
                .toSet()

            val wheres = buildList {
                values.forEach {
                    add(Where(internalName, operator, it, whereOperator))
                }
            }

            query.where(wheres)
            return
        }

        query.where(
            internalName,
            operator,
            transform(value)
        )
    }

    override fun transform(value: String): T {
        try {
            return classEnum.valueOf(value)
        } catch (e: Exception) {
            throw FilterException("invalid value")
        }
    }
}
