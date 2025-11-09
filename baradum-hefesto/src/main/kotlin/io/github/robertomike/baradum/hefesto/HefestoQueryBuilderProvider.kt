package io.github.robertomike.baradum.hefesto

import io.github.robertomike.baradum.core.interfaces.QueryBuilder
import io.github.robertomike.baradum.core.interfaces.QueryBuilderProvider
import io.github.robertomike.hefesto.models.BaseModel

/**
 * ServiceLoader provider for Hefesto QueryBuilder implementation.
 * This is automatically discovered by BaradumCore.make() via Java's ServiceLoader.
 */
class HefestoQueryBuilderProvider : QueryBuilderProvider {

    override fun <T> create(modelClass: Class<T>): QueryBuilder<T> {
        @Suppress("UNCHECKED_CAST")
        return HefestoQueryBuilder(modelClass as Class<out BaseModel>) as QueryBuilder<T>
    }

    override fun getName(): String = "hefesto"

    override fun supports(modelClass: Class<*>): Boolean {
        return BaseModel::class.java.isAssignableFrom(modelClass)
    }
}