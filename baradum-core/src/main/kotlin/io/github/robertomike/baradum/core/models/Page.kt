package io.github.robertomike.baradum.core.models

/**
 * Generic pagination result container
 */
data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val limit: Int,
    val offset: Long
) {
    val totalPages: Long
        get() = if (limit > 0) (totalElements + limit - 1) / limit else 0
    
    val currentPage: Long
        get() = if (limit > 0) offset / limit else 0
    
    val hasNext: Boolean
        get() = (currentPage + 1) * limit < totalElements
    
    val hasPrevious: Boolean
        get() = currentPage > 0
}
