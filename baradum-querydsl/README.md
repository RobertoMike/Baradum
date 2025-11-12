# Baradum QueryDSL Module

QueryDSL implementation for the Baradum filtering library. This module provides seamless integration between Baradum's powerful filtering system and QueryDSL's type-safe query API with convenient extension functions.

## Features

- ✅ Full QueryDSL integration with type-safe queries
- ✅ **Extension functions** for direct Q-class usage (`QUser.user.baradum(...)`)
- ✅ All Baradum operators supported (EQUAL, DIFF, GREATER, LESS, LIKE, IN, IS_NULL, BETWEEN, etc.)
- ✅ Kotlin property reference support for type-safe filtering
- ✅ Pagination with total count
- ✅ Sorting (ASC/DESC)
- ✅ AND/OR logical operators
- ✅ Works with JPA/Hibernate entities
- ✅ **81 comprehensive integration and unit tests**

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.robertomike:baradum-querydsl:3.0.0")
}
```

## Requirements

- QueryDSL 5.0.0+
- Jakarta Persistence API 3.1.0+
- JPA/Hibernate for entity management
- Generated Q-classes (QueryDSL APT processor)

## Quick Start

### 1. Setup QueryDSL APT Processor

Configure kapt to generate Q-classes:

```kotlin
plugins {
    kotlin("kapt")
}

dependencies {
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}
```

### 2. Define Your Entity

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "name")
    val name: String,
    
    @Column(name = "email")
    val email: String,
    
    @Column(name = "age")
    val age: Int,
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    val status: UserStatus
)

enum class UserStatus {
    ACTIVE, INACTIVE, PENDING
}
```

### 3. Use Extension Functions (Recommended)

The easiest way to use Baradum with QueryDSL is through extension functions:

```kotlin
import io.github.robertomike.baradum.querydsl.extensions.*
import io.github.robertomike.baradum.core.filters.*

class UserService(private val entityManager: EntityManager) {
    
    fun findUsers(request: HttpServletRequest): List<User> {
        // Use the baradum() extension function directly on Q-classes
        return QUser.user
            .baradum(entityManager)
            .allowedFilters(
                ExactFilter(User::name),
                PartialFilter(User::email),
                GreaterFilter(User::age, orEqual = true)
            )
            .allowedSort("name", "age")
            .withParams(request.parameterMap.mapValues { it.value.first() })
            .get()
    }
    
    // Or use the queryBuilder() extension for direct QueryBuilder access
    fun findActiveUsers(): List<User> {
        return QUser.user
            .queryBuilder(entityManager)
            .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
            .orderBy("name", SortDirection.ASC)
            .get()
    }
}
```

### 4. Alternative: Traditional Factory Approach

You can also use the traditional factory pattern:

```kotlin
import io.github.robertomike.baradum.querydsl.QueryDslBaradum

class UserService(private val entityManager: EntityManager) {
    
    fun findUsers(request: HttpServletRequest): List<User> {
        val baradum = QueryDslBaradum.make(QUser.user, entityManager)
            .allowedFilters(
                ExactFilter(User::name),
                PartialFilter(User::email)
            )
            .allowedSort("name", "age")
        
        return baradum.withParams(request.parameterMap.mapValues { it.value.first() }).get()
    }
}
```

## Extension Functions

The module provides convenient extension functions for `EntityPathBase<T>` (Q-classes):

### `baradum()` Extensions

Create a Baradum instance directly from a Q-class:

```kotlin
// Basic usage
QUser.user.baradum(entityManager)

// With pre-configured filters (vararg)
QUser.user.baradum(
    entityManager,
    ExactFilter(User::name),
    GreaterFilter(User::age, orEqual = true)
)

// With filters list
val filters = listOf(ExactFilter(User::name))
QUser.user.baradum(entityManager, filters)
```

### `queryBuilder()` Extension

Get direct access to the QueryDslQueryBuilder:

```kotlin
val users = QUser.user
    .queryBuilder(entityManager)
    .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
    .orderBy("name", SortDirection.ASC)
    .get()
```

## Complete Usage Examples

All Baradum filters work with QueryDSL:

```kotlin
// Exact match
ExactFilter(User::status)

// Partial string match (LIKE)
PartialFilter(User::name)

// Comparison filters
GreaterFilter(User::age, orEqual = true)  // age >= value
LessFilter(User::age, orEqual = false)    // age < value

// Date filters
DateFilter.forLocalDate("createdAt")
DateFilter.forLocalDateTime("updatedAt")

// IN filter
InFilter("status")

// NULL checks
IsNullFilter("deletedAt")

// Search filter (multiple fields)
SearchFilter("search", listOf("name", "email"))

// Interval/Range filter
IntervalFilter("age")
```

## Usage Examples

### Basic Filtering

```kotlin
// GET /users?name=John&status=ACTIVE
val baradum = QueryDslBaradum.make(QUser.user, entityManager)
    .allowedFilters(
        ExactFilter(User::name),
        ExactFilter(User::status)
    )

val users = baradum.applyFilters(request).get()
```

### Filtering with Sorting

```kotlin
// GET /users?minAge=18&sort=name,-age
val baradum = QueryDslBaradum.make(QUser.user, entityManager)
    .allowedFilters(
        GreaterFilter(User::age, "minAge", orEqual = true)
    )
    .allowedSort("name", "age", "createdAt")

val users = baradum.applyFilters(request).get()
```

### Pagination

```kotlin
// GET /users?page=2&limit=20
val baradum = QueryDslBaradum.make(QUser.user, entityManager)
    .allowedFilters(ExactFilter(User::status))

val page = baradum.applyFilters(request).page(20, 20) // limit, offset
println("Total: ${page.totalElements}")
println("Users: ${page.content}")
```

### Advanced: Custom Predicates

You can access the underlying QueryDSL query for advanced operations:

```kotlin
val queryBuilder = QueryDslQueryBuilder(QUser.user, entityManager)

// Apply Baradum filters
queryBuilder
    .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
    .where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)
    .orderBy("name", SortDirection.ASC)

// Get the underlying QueryDSL query for custom operations
val query = queryBuilder.getQuery()
query.where(QUser.user.email.endsWith("@example.com"))

val users = queryBuilder.get()
```

## Kotlin Property References

Use type-safe Kotlin property references instead of strings:

```kotlin
// Type-safe ✅
ExactFilter(User::name)
GreaterFilter(User::age, orEqual = true)

// String-based (also works but not type-safe)
ExactFilter("name")
GreaterFilter("age", orEqual = true)
```

Benefits:
- Compile-time field validation
- Refactoring support
- IDE auto-completion
- No typos in field names

## Operators Mapping

| Baradum Operator | QueryDSL Ops | Description |
|-----------------|--------------|-------------|
| EQUAL | EQ | Equals |
| DIFF | NE | Not equals |
| GREATER | GT | Greater than |
| GREATER_OR_EQUAL | GOE | Greater or equal |
| LESS | LT | Less than |
| LESS_OR_EQUAL | LOE | Less or equal |
| LIKE | LIKE | String pattern match |
| NOT_LIKE | LIKE (negated) | Not matching pattern |
| IN | IN | In list |
| NOT_IN | NOT_IN | Not in list |
| IS_NULL | IS_NULL | Is null |
| IS_NOT_NULL | IS_NOT_NULL | Is not null |
| BETWEEN | BETWEEN | Between two values |

## Configuration

### With EntityManager

```kotlin
val baradum = QueryDslBaradum.make(QUser.user, entityManager)
```

### With JPAQueryFactory

```kotlin
val queryFactory = JPAQueryFactory(entityManager)
val baradum = QueryDslBaradum.make(QUser.user, queryFactory)
```

### With Filters Pre-configured

```kotlin
val filters = listOf(
    ExactFilter(User::name),
    GreaterFilter(User::age, orEqual = true)
)

val baradum = QueryDslBaradum.make(QUser.user, entityManager, filters)
```

## Testing

The module includes comprehensive tests:

- **16 converter tests** - Verify all operator/sort conversions
- **5 structure tests** - Verify interface implementations and enums

Run tests:
```bash
./gradlew :baradum-querydsl:test
```

## Comparison with Hefesto Module

| Feature | Hefesto | QueryDSL |
|---------|---------|----------|
| Type Safety | ✅ | ✅ |
| JPA Support | ✅ | ✅ |
| Custom Queries | Limited | Full QueryDSL power |
| Learning Curve | Low | Medium |
| IDE Support | Good | Excellent |
| Q-classes Required | ❌ | ✅ |

## Troubleshooting

### Q-classes Not Generated

Ensure kapt is configured properly:

```kotlin
plugins {
    kotlin("kapt")
}

kapt {
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
}

dependencies {
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}
```

### Type Mismatch Errors

Make sure your entities use Jakarta Persistence annotations (jakarta.persistence.*), not javax.persistence.*.

## License

MIT License - Same as Baradum core

## Links

- [Baradum Core Documentation](../DOCUMENTATION.md)
- [QueryDSL Documentation](http://querydsl.com/)
- [Filter API Reference](../FILTER_API_REFERENCE.md)
