# Baradum QueryDSL Module

QueryDSL implementation of Baradum's `QueryBuilder`, for type-safe queries against JPA/Hibernate entities via generated Q-classes.

See [../DOCUMENTATION.md](../DOCUMENTATION.md) for the general Baradum guide and [../FILTER_API_REFERENCE.md](../FILTER_API_REFERENCE.md) for the full filter reference — this file covers only what's specific to QueryDSL.

## Requirements

- QueryDSL 5.0.0+ (`jakarta` classifier)
- Jakarta Persistence API 3.1.0+
- JPA/Hibernate entity manager
- Generated Q-classes (via the QueryDSL APT processor)

## Installation

```kotlin
plugins {
    kotlin("kapt") // needed to generate Q-classes
}

dependencies {
    implementation("io.github.robertomike:baradum-querydsl:3.1.0")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
}

kapt {
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
}
```

## Quick start

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    val email: String,
    val age: Int,
    @Enumerated(EnumType.STRING)
    val status: UserStatus,
)

enum class UserStatus { ACTIVE, INACTIVE, PENDING }
```

The APT processor generates `QUser` for this entity. Two ways to build a query from it:

### 1. Extension functions (recommended)

```kotlin
import io.github.robertomike.baradum.querydsl.extensions.baradum
import io.github.robertomike.baradum.core.filters.*

class UserService(private val entityManager: EntityManager) {

    fun findUsers(params: Map<String, String>): List<User> =
        QUser.user
            .baradum(entityManager)
            .allowedFilters(
                ExactFilter(User::name),
                PartialFilter(User::email),
                GreaterFilter(User::age, orEqual = true),
            )
            .allowedSort("name", "age")
            .withParams(params)
            .get()
}
```

`withParams(map)` is how you feed request parameters in — there's no `applyFilters(request)` method; filtering happens automatically inside `.get()` / `.page()` / `.findFirst()` once params are set (via `withParams`/`withParam`, or the shared `Baradum.request` static field — see [../DOCUMENTATION.md#configuration](../DOCUMENTATION.md#configuration)).

### 2. `QueryDslBaradum` factory

```kotlin
import io.github.robertomike.baradum.querydsl.QueryDslBaradum

val users = QueryDslBaradum.make(QUser.user, entityManager)
    .allowedFilters(ExactFilter(User::name), PartialFilter(User::email))
    .allowedSort("name", "age")
    .withParams(params)
    .get()
```

Both forms accept an `EntityManager` or a `JPAQueryFactory`, and an optional pre-built filter list:

```kotlin
val filters = listOf(ExactFilter(User::name), GreaterFilter(User::age, orEqual = true))

QUser.user.baradum(entityManager, filters)               // List<Filter<*, *>>
QUser.user.baradum(entityManager, ExactFilter(User::name), PartialFilter(User::email)) // vararg
QueryDslBaradum.make(QUser.user, entityManager, filters)
```

### Direct query builder access

Skip Baradum's filter DSL entirely and drive `QueryDslQueryBuilder` yourself:

```kotlin
val users = QUser.user
    .queryBuilder(entityManager)
    .where("age", BaradumOperator.GREATER_OR_EQUAL, 18)
    .orderBy("name", SortDirection.ASC)
    .get()
```

## Pagination

```kotlin
val page = QUser.user.baradum(entityManager)
    .allowedFilters(ExactFilter(User::status))
    .withParams(params)
    .page(20, 40) // limit, offset

println("Total: ${page.totalElements}, rows: ${page.content.size}")
```

## Advanced: dropping to raw QueryDSL

`QueryDslQueryBuilder.getQuery()` returns the underlying `JPAQuery<T>` for anything Baradum's filter DSL doesn't cover:

```kotlin
val builder = QUser.user.queryBuilder(entityManager)
builder.where("status", BaradumOperator.EQUAL, UserStatus.ACTIVE)

val query = builder.getQuery()
query.where(QUser.user.email.endsWith("@example.com")) // raw QueryDSL predicate, combined with the above

val users = builder.get()
```

## Filters that work here

All of `baradum-core`'s filters work unchanged against QueryDSL — see [../FILTER_API_REFERENCE.md](../FILTER_API_REFERENCE.md) for the full list and which ones accept Kotlin property references. For a lambda-based custom filter, use the generic `io.github.robertomike.baradum.core.filters.CustomFilter<QueryDslQueryBuilder<*>>` (works with any backend), or subclass `Filter<T, QueryDslQueryBuilder<*>>` directly for anything more involved (see [../DOCUMENTATION.md#custom-filters](../DOCUMENTATION.md#custom-filters)).

## Operator mapping

| `BaradumOperator` | QueryDSL `Ops` |
|---|---|
| `EQUAL` | `EQ` |
| `DIFF` | `NE` |
| `GREATER` | `GT` |
| `GREATER_OR_EQUAL` | `GOE` |
| `LESS` | `LT` |
| `LESS_OR_EQUAL` | `LOE` |
| `LIKE` | `LIKE` |
| `NOT_LIKE` | `LIKE`, negated |
| `LIKE_IGNORE_CASE` | `LIKE_IC` |
| `IN` | `IN` |
| `NOT_IN` | `NOT_IN` |
| `IS_NULL` | `IS_NULL` |
| `IS_NOT_NULL` | `IS_NOT_NULL` |
| `BETWEEN` | `BETWEEN` |

## Performance note

Field-path lookups (mapping a filter's field name to a QueryDSL `Path`) are cached process-wide in a `ConcurrentHashMap` keyed by `(entity class, field name)`, so repeated queries against the same entity don't pay reflection cost on every request.

## Comparison with Hefesto

| | Hefesto | QueryDSL |
|---|---|---|
| Type safety | Runtime field names | Compile-time-checked Q-classes |
| Setup | No codegen | Requires the QueryDSL APT processor |
| Custom queries | `CustomFilter` lambda (Hefesto-typed or generic) | `CustomFilter` lambda (generic), or full QueryDSL API via `getQuery()` |
| Spring Boot auto-config | ✅ via `baradum-apache-tomcat` | ❌ wire the request manually |

## Troubleshooting

**Q-classes not generated** — confirm `kapt` is applied and the APT dependency is present:

```kotlin
plugins { kotlin("kapt") }
dependencies { kapt("com.querydsl:querydsl-apt:5.0.0:jakarta") }
```

**Type mismatch errors** — make sure entities use `jakarta.persistence.*` annotations, not `javax.persistence.*`.

## License

MIT — same as the rest of Baradum.
