package io.github.robertomike.baradum.hefesto

import io.github.robertomike.hefesto.models.BaseModel

/**
 * Factory for creating Baradum instances with Hefesto backend.
 *
 * Named to match [io.github.robertomike.baradum.querydsl.QueryDslBaradum] - previously this was
 * also just called `Baradum`, which was easy to confuse with the generic, ServiceLoader-based
 * [io.github.robertomike.baradum.core.Baradum.make]. See [Baradum] (this package) for the
 * deprecated old name, kept for compatibility.
 */
object HefestoBaradum {
    /**
     * Creates a new Baradum instance with Hefesto query builder
     */
    @JvmStatic
    fun <T : BaseModel> make(modelClass: Class<T>): io.github.robertomike.baradum.core.Baradum<T, HefestoQueryBuilder<T>> {
        val queryBuilder = HefestoQueryBuilder(modelClass)
        return io.github.robertomike.baradum.core.Baradum(queryBuilder)
    }
}

/**
 * Deprecated: renamed to [HefestoBaradum] for consistency with
 * [io.github.robertomike.baradum.querydsl.QueryDslBaradum]. Kept as a thin delegate so existing
 * code keeps compiling.
 */
@Deprecated(
    message = "Renamed to HefestoBaradum for consistency with QueryDslBaradum",
    replaceWith = ReplaceWith("HefestoBaradum", "io.github.robertomike.baradum.hefesto.HefestoBaradum")
)
object Baradum {
    @JvmStatic
    fun <T : BaseModel> make(modelClass: Class<T>) = HefestoBaradum.make(modelClass)
}
