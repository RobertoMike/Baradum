# Baradum — Complete Guide

This is the full user guide for Baradum. For a quick pitch and install snippet, see [README.md](README.md). For exhaustive per-filter constructor signatures, see [FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md).

## Table of Contents

- [Installation](#installation)
- [Choosing a backend](#choosing-a-backend)
- [Quick start](#quick-start)
- [Filters](#filters)
- [Kotlin property references](#kotlin-property-references)
- [Date filtering](#date-filtering)
- [Sorting](#sorting)
- [Body-based filtering](#body-based-filtering)
- [Pagination and results](#pagination-and-results)
- [Custom filters](#custom-filters)
- [Configuration](#configuration)
- [Known limitations](#known-limitations)

## Installation

Current version: **4.0.0**. Pick one query backend module:

```kotlin
dependencies {
    // Hefesto (Hibernate/HefestoSQL)
    implementation("io.github.robertomike:baradum-hefesto:4.0.0")

    // OR QueryDSL (type-safe, requires generated Q-classes)
    implementation("io.github.robertomike:baradum-querydsl:4.0.0")

    // Optional: auto-wires the current HttpServletRequest for Spring Boot 3 + Hefesto
    implementation("io.github.robertomike:baradum-apache-tomcat:4.0.0")
}
```

```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-hefesto</artifactId>
    <version>4.0.0</version>
</dependency>
```

`baradum-core` is a transitive dependency of both backends — you don't add it directly.

## Choosing a backend

Baradum's core module has no idea how to talk to a database; it only knows how to turn filters into calls on a `QueryBuilder<T>`. Each backend module supplies that implementation, and each ships its **own** factory:

| | Hefesto | QueryDSL |
|---|---|---|
| Entry point | `HefestoBaradum.make(User::class.java)` | `QueryDslBaradum.make(QUser.user, entityManager)` or `QUser.user.baradum(entityManager)` |
| Requires | Your entity extends Hefesto's `BaseModel` | A generated Q-class for the entity (QueryDSL APT) |
| Custom lambda filter | `io.github.robertomike.baradum.hefesto.filters.CustomFilter`, or the generic `io.github.robertomike.baradum.core.filters.CustomFilter` | the same generic `core.filters.CustomFilter`, or write a `Filter<T, QueryDslQueryBuilder<*>>` subclass |

> **Watch the imports.** `io.github.robertomike.baradum.hefesto.HefestoBaradum` is named to match `QueryDslBaradum`. It used to just be called `Baradum` — that name is kept as a deprecated compatibility shim, but it was easy to confuse with the generic, ServiceLoader-based `io.github.robertomike.baradum.core.Baradum` shown below, so prefer `HefestoBaradum` going forward. QueryDSL has no such shortcut class; always go through `QueryDslBaradum` or the `EntityPathBase` extension functions.

There's also a generic, backend-agnostic factory on the **core** `Baradum` class:

```kotlin
import io.github.robertomike.baradum.core.Baradum

Baradum.make(User::class.java) // resolves a backend via ServiceLoader
```

This uses Java's `ServiceLoader` to find whichever `QueryBuilderProvider` is on your classpath and `supports()` your model class. Today, **only `baradum-hefesto` registers a provider** (it requires your model to extend Hefesto's `BaseModel`); `baradum-querydsl` has no `ServiceLoader` provider because a QueryDSL query builder also needs a Q-class and an `EntityManager`/`JPAQueryFactory`, which a bare `Class<T>` can't supply. Practically:

- **Hefesto users** can use either `io.github.robertomike.baradum.core.Baradum.make(User::class.java)` or `HefestoBaradum.make(User::class.java)` — same result, the latter just skips the `ServiceLoader` lookup.
- **QueryDSL users** must always use `QueryDslBaradum.make(...)` or the `.baradum(...)` extension function.

## Quick start

**Without Baradum:**

```java
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) Integer minAge,
    @RequestParam(required = false) Integer maxAge
) {
    if (categoryId != null && minAge != null && maxAge != null) {
        return repository.findByCategoryIdAndAgeGreaterThanEqualAndAgeLessThanEqual(categoryId, minAge, maxAge);
    }
    // ... every other combination
    return repository.findAll();
}
```

**With Baradum:**

```java
@GetMapping("/users")
public List<User> getUsers() {
    return Baradum.make(User.class)
        .allowedFilters("categoryId")
        .allowedFilters(new IntervalFilter("age"))
        .get();
}
```

```kotlin
@GetMapping("/users")
fun getUsers(): List<User> =
    Baradum.make(User::class.java)
        .allowedFilters(ExactFilter(User::categoryId), IntervalFilter(User::age))
        .allowedSort(User::name, User::createdAt)
        .get()
```

`allowedFilters("categoryId")` is shorthand: a bare string is turned into an `ExactFilter` for that field. Anything more specific (ranges, LIKE, enums, dates...) needs an explicit filter instance.

## Filters

Every filter is a small class that reads one request parameter and translates it into a `where(...)` call. Below is a practical overview of each; see [FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md) for every constructor and edge case.

### ExactFilter — exact match, auto type conversion

```kotlin
ExactFilter(User::status)              // ?status=ACTIVE  -> status = 'ACTIVE'
ExactFilter(User::isActive)            // ?isActive=true  -> isActive = true (Boolean)
ExactFilter("userId", "user_id")       // ?userId=123     -> user_id = 123
```

Automatically converts `"true"`/`"false"` to `Boolean`, digit strings to `Int`/`Long`, and decimal strings to `Double`; anything else is passed through as a `String` (the ORM/DB layer handles enum columns).

### PartialFilter — LIKE search

```kotlin
PartialFilter(User::username)                                   // ?username=john -> LIKE 'john%'
PartialFilter(User::email, "search").setStrategy(SearchLikeStrategy.COMPLETE) // -> LIKE '%john%'
PartialFilter(User::username).setIgnoreCase(true)                // ?username=JOHN matches 'john'
```

Strategies: `FINAL` (`value%`, default), `START` (`%value`), `COMPLETE` (`%value%`). If the incoming value already contains a `%`, it's used as-is. `setIgnoreCase(true)` matches case-insensitively (QueryDSL natively, Hefesto via a `LOWER()`-wrapped predicate).

### SearchFilter — OR search across multiple fields

```java
SearchFilter.of("name", "email", "phone")           // param defaults to "search"
new SearchFilter("q", "title", "description")       // custom param name
```

```
?search=john -> name LIKE '%john%' OR email LIKE '%john%' OR phone LIKE '%john%'
```

No Kotlin property-reference constructor — field names are always plain strings here. Has the same `setIgnoreCase(true)` option as `PartialFilter`, applied to every searched field.

### EnumFilter — enum values, single or `IN`

```java
EnumFilter("status", Status.class)          // ?status=ACTIVE          -> status = ACTIVE
                                             // ?status=ACTIVE,PENDING  -> status IN (ACTIVE, PENDING)
```

```kotlin
EnumFilter(User::status, Status::class.java)   // property reference - enum class is still required
```

The enum class is always required explicitly — Kotlin can't infer it from a property reference alone.

### IntervalFilter — numeric ranges

```java
IntervalFilter("age")
// ?age=25       -> age = 25
// ?age=18-65    -> age >= 18 AND age <= 65
// ?age=18,65    -> same, comma accepted for backward compatibility
// ?age=18-      -> age >= 18
// ?age=-65      -> age <= 65 (treated as "max only" - see FILTER_API_REFERENCE.md for the negative-number caveat)
// ?age=-10--5   -> age >= -10 AND age <= -5
```

### InFilter / NotInFilter — `IN` / `NOT IN` with a delimiter

```kotlin
InFilter("country")                          // ?country=US,CA,MX -> country IN ('US','CA','MX')
InFilter("tags", delimiter = "|")            // ?tags=a|b|c       -> tags IN ('a','b','c')
NotInFilter("excludedCountries", "country")  // ?excludedCountries=US,CA -> country NOT IN ('US','CA')
```

The delimiter is fixed at construction (third constructor argument) — there's no `setDelimiter()` method. `NotInFilter` is the exact mirror of `InFilter`.

### IsNullFilter — NULL / NOT NULL

```java
IsNullFilter("deletedAt")
// ?deletedAt=null, true, 1, yes         -> IS NULL
// ?deletedAt=not_null, false, 0, no     -> IS NOT NULL
```

### ComparisonFilter — one param, any prefix operator

```java
ComparisonFilter("price")
// ?price=>100   -> price > 100
// ?price=>=50   -> price >= 50
// ?price=<1000  -> price < 1000
// ?price=<=500  -> price <= 500
// ?price=!=0    -> price != 0
// ?price=100    -> price = 100 (no prefix)
```

### GreaterFilter / LessFilter — dedicated single-direction comparisons

```kotlin
GreaterFilter(User::age)                              // ?age=18 -> age > 18
GreaterFilter(User::age, "minAge", orEqual = true)     // ?minAge=18 -> age >= 18
LessFilter(User::age, "maxAge", orEqual = true)        // ?maxAge=65 -> age <= 65
```

Values are auto-parsed as `Int`, `Long` (if out of `Int` range), or `Double` (if it contains a `.`); falls back to string comparison otherwise.

### CustomFilter — your own logic

```kotlin
// Generic - works with any backend
CustomFilter<QueryDslQueryBuilder<*>>("status") { query, value ->
    if (value == "premium") {
        query.where("subscription_level", BaradumOperator.GREATER, 5)
    } else {
        query.where("status", BaradumOperator.EQUAL, value)
    }
}
```

```java
// Hefesto-specific, slightly terser for Hefesto-only code
new CustomFilter<>("status", (query, value) -> {
    if (value.equals("premium")) {
        query.where("subscription_level", BaradumOperator.GREATER, 5);
    } else {
        query.where("status", BaradumOperator.EQUAL, value);
    }
})
```

`io.github.robertomike.baradum.core.filters.CustomFilter` works with any `QueryBuilder<*>` backend. `baradum-hefesto`'s own `io.github.robertomike.baradum.hefesto.filters.CustomFilter` is pre-typed to `HefestoQueryBuilder` — pick whichever fits.

### Default values and ignored values (all filters)

```java
ExactFilter("status").setDefaultValue("ACTIVE")   // used when ?status is absent
IntervalFilter("age").addIgnore("0", "null", "")  // filter is skipped if the value matches one of these
```

## Kotlin property references

Every built-in filter except `SearchFilter` and `CustomFilter` accepts a Kotlin property reference (`User::name`) in place of a string field name, for compile-time checking and rename-safe refactors:

**`ExactFilter`, `PartialFilter`, `GreaterFilter`, `LessFilter`, `DateFilter`, `IntervalFilter`, `InFilter`, `NotInFilter`, `IsNullFilter`, `ComparisonFilter`, `EnumFilter`**

```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val status: Status,
    val createdAt: LocalDateTime
)

Baradum.make(User::class.java)
    .allowedFilters(
        ExactFilter(User::id),
        PartialFilter(User::name, "search"),
        GreaterFilter(User::age, "minAge", orEqual = true),
        LessFilter(User::age, "maxAge", orEqual = true),
        DateFilter.forLocalDateTime(User::createdAt),
        IntervalFilter(User::age),
        InFilter(User::status),
        EnumFilter(User::status, Status::class.java),
    )
    .allowedSort(User::name, User::createdAt)
    .get()
```

`SearchFilter` takes multiple field names via vararg, which doesn't map onto a single property reference, so it stays string-based. Neither `CustomFilter` accepts one either — the param is always a plain request-parameter name.

Every property-reference-capable filter also has factory methods, e.g. `ExactFilter.of(User::status)`, `GreaterFilter.of(User::age, orEqual = true)`, `EnumFilter.of(User::status, Status::class.java)`.

## Date filtering

`DateFilter` supports five date/time types, each with its own default pattern:

| Type | Java type | Default pattern |
|---|---|---|
| `LOCAL_DATE` (default) | `java.time.LocalDate` | `yyyy-MM-dd` |
| `LOCAL_DATE_TIME` | `java.time.LocalDateTime` | `yyyy-MM-dd'T'HH:mm:ss` |
| `UTIL_DATE` | `java.util.Date` | `yyyy-MM-dd` |
| `SQL_DATE` | `java.sql.Date` | `yyyy-MM-dd` |
| `SQL_TIMESTAMP` | `java.sql.Timestamp` | `yyyy-MM-dd HH:mm:ss` |

```kotlin
DateFilter("createdAt")                                       // LocalDate, default pattern
DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")
DateFilter.forUtilDate(User::birthDate, "MM-dd-yyyy")

DateFilter.builder("eventDate")
    .useLocalDate()
    .withPattern("yyyy/MM/dd")
    .build()
```

Accepted value formats:

| Format | Meaning | Example |
|---|---|---|
| `2024-01-01` | exact match | `date = '2024-01-01'` |
| `2024-01-01\|2024-12-31` | range (pipe) | `date >= ... AND date <= ...` |
| `>2024-01-01`, `>=`, `<`, `<=`, `<>` | comparison prefix | `date > '2024-01-01'` etc. |

## Sorting

```java
Baradum.make(User.class)
    .allowedSort("name", "createdAt")
    .allowedSort(new OrderBy("alias", "actual_column"))  // expose a friendly name for a differently-named column
    .get();
```

```kotlin
Baradum.make(User::class.java)
    .allowedSort(User::name, User::createdAt)
    .get()
```

- `?sort=name` → `ORDER BY name ASC`
- `?sort=-name` → `ORDER BY name DESC`
- `?sort=name,-createdAt` → both, applied in the order given

Requesting a sort field that isn't in `allowedSort(...)` throws `SortableException`.

## Body-based filtering

For filter combinations too complex for a query string, POST a JSON body and call `.useBody()` (or `.useOnlyBody()` to reject GET/query-string filtering entirely):

```kotlin
Baradum.make(User::class.java)
    .allowedFilters(ExactFilter(User::id), PartialFilter(User::name))
    .useBody()
    .get()
```

Body shape (matches `BodyRequest` / `FilterRequest` / `OrderRequest` exactly):

```json
{
    "filters": [
        { "field": "id", "value": "1", "operator": "EQUAL" },
        { "field": "name", "value": "abc%", "operator": "LIKE", "type": "OR" },
        { "field": "status", "operator": "IS_NULL", "type": "AND" },
        {
            "subFilters": [
                { "field": "status", "value": "ACTIVE,INACTIVE", "operator": "IN", "type": "OR" }
            ]
        }
    ],
    "sorts": [
        { "field": "id" },
        { "field": "name", "sort": "DESC" }
    ]
}
```

- `operator` is one of the `BaradumOperator` values: `EQUAL`, `DIFF`, `GREATER`, `GREATER_OR_EQUAL`, `LESS`, `LESS_OR_EQUAL`, `LIKE`, `NOT_LIKE`, `LIKE_IGNORE_CASE`, `IN`, `NOT_IN`, `IS_NULL`, `IS_NOT_NULL`, `BETWEEN` (defaults to `EQUAL`). `BETWEEN` expects exactly two comma-separated values in `value` (e.g. `"25,33"`); anything else throws `FilterException`. See [FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md#the-between-operator) — no built-in filter emits `BETWEEN` directly, but it's fully supported here.
- `type` (how this condition joins the previous one) is `AND` or `WhereOperator.OR` (defaults to `AND`).
- `field` must match a filter's **`param`**, not its `internalName` — the request is only ever allowed to name filters you declared with `allowedFilters(...)`; anything else throws `FilterException`.
- `subFilters` nests recursively for grouped conditions.
- Every built-in filter supports body operation by default (`Filter.supportBodyOperation()` returns `true` unless a filter overrides it — none of the built-ins do).

## Pagination and results

`Baradum<T, Q>` exposes three terminal operations, all of which apply your filters/sort first:

```kotlin
baradum.get()                 // List<T> — everything matching
baradum.page(20)              // Page<T> — first 20 (offset 0)
baradum.page(20, 40)          // Page<T> — 20 rows starting at offset 40
baradum.findFirst()           // Optional<T> — first matching row, or empty
```

`Page<T>` carries `content`, `totalElements`, `limit`, `offset`, plus derived `totalPages`, `currentPage`, `hasNext`, `hasPrevious`.

If you called `.withParams(map)` and that map contains `"limit"`/`"offset"` keys, `.page(limit, offset)` prefers those over the arguments you passed in code.

## Custom filters

For anything the built-in filters don't cover, subclass `Filter<T, Q>` directly:

```kotlin
class RatingFilter<Q : QueryBuilder<*>>(
    param: String,
    internalName: String = param
) : Filter<Double, Q>(param, internalName) {

    override fun filterByParam(query: Q, value: String) {
        query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, value.toDouble())
    }

    override fun transform(value: String): Double = value.toDouble()
}
```

For simple lambda logic, the ready-made `CustomFilter` avoids the subclass entirely — see [Filters](#customfilter--your-own-logic) above.

## Configuration

There are two ways Baradum learns about the current request's parameters. **Prefer the explicit one** — it has no global mutable state and works identically on every framework:

### `withParams` / `withParam` (recommended)

```kotlin
Baradum.make(User::class.java)
    .allowedFilters(ExactFilter(User::name))
    .withParams(request.parameterMap.mapValues { it.value.first() }) // Map<String, String>
    .get()
```

### Static request wiring (`Baradum.request`, deprecated)

If you don't call `withParams(...)`, Baradum falls back to a shared static field, `Baradum.request: BasicRequest<*>?`, that you (or an auto-configuration) set once. This field is marked `@Deprecated` in favor of `withParams(...)`/`withParam(...)` above, but remains fully functional — it's still how `baradum-apache-tomcat`'s auto-configuration wires things up (see below), which is the one case where relying on it is still the sanctioned pattern. This is what `baradum-apache-tomcat` does for you automatically on Spring Boot 3 + Hefesto: it registers `AutoConfigurationSpring3` as a Spring auto-configuration bean that receives Spring's request-scoped `HttpServletRequest` proxy and assigns it to `Baradum.request` — no extra code needed on your end, and it stays correct across concurrent requests because Spring injects a thread-aware proxy, not the request object itself.

For anything else — Spring Boot 2 / Tomcat 9, a non-Spring framework, or the QueryDSL module without `baradum-apache-tomcat` — implement `BasicRequest` yourself and set the field per-request:

```kotlin
class MyCustomRequest(httpRequest: HttpServletRequest) : BasicRequest<HttpServletRequest>(httpRequest) {
    override fun findParamByName(name: String): String? = request.getParameter(name)
    override val method: String get() = request.method
    override val json: String get() = request.reader.readText()
}

// once per request, before calling .get()/.page()/.findFirst()
Baradum.request = MyCustomRequest(httpServletRequest)
```

(From Java: `Baradum.setRequest(new MyCustomRequest(request));`.)

Because this is a single static field shared process-wide, only rely on it when you're certain your framework hands you a per-request-safe value (a scoped proxy, as Spring does) — otherwise prefer `withParams(...)`.

## Known limitations

- No automatic Swagger/OpenAPI generation for filter parameters.
- `baradum-apache-tomcat` wires only the Hefesto backend, and only for Jakarta Servlet / Spring Boot 3. There is currently no equivalent module for Spring Boot 2, Tomcat 9, or QueryDSL — use `withParams(...)` or a custom `BasicRequest` in those cases.
- Filter field names in body requests (`FilterRequest.field`) must match a declared filter's `param`; they are validated against `allowedFilters(...)` at request time, not compile time.

---

See also: [FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md) · [QUICK_REFERENCE.md](QUICK_REFERENCE.md) · [baradum-querydsl/README.md](baradum-querydsl/README.md) · [CHANGELOG.md](CHANGELOG.md)
