package io.github.robertomike.baradum.hefesto

import io.github.robertomike.baradum.core.Baradum
import io.github.robertomike.hefesto.models.BaseModel

/**
 * Factory for creating Baradum instances with Hefesto backend
 */
object Baradum {
    /**
     * Creates a new Baradum instance with Hefesto query builder
     */
    @JvmStatic
    fun <T : BaseModel> make(modelClass: Class<T>): Baradum<T, HefestoQueryBuilder<T>> {
        val queryBuilder = HefestoQueryBuilder(modelClass)
        return Baradum(queryBuilder)
    }
}
