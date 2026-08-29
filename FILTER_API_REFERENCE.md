# Baradum Filter API Reference

Exhaustive constructor and behavior reference for every filter in `baradum-core` (plus the Hefesto-only `CustomFilter`). For narrative examples and how filters fit into a full request, see [DOCUMENTATION.md](DOCUMENTATION.md).

## Table of Contents

- [Base Filter class](#base-filter-class)
- [ExactFilter](#exactfilter)
- [PartialFilter](#partialfilter)
- [SearchFilter](#searchfilter)
- [EnumFilter](#enumfilter)
- [IntervalFilter](#intervalfilter)
- [InFilter](#infilter)
- [IsNullFilter](#isnullfilter)
- [ComparisonFilter](#comparisonfilter)
- [GreaterFilter](#greaterfilter)
- [LessFilter](#lessfilter)
- [DateFilter](#datefilter)
- [CustomFilter (Hefesto module)](#customfilter-hefesto-module)
- [Writing your own filter](#writing-your-own-filter)
- [Errors](#errors)

---

## Base Filter class

`Filter<T, Q : QueryBuilder<*>>` — every filter extends this.

### Constructors

```kotlin
// declared on every concrete filter individually — signature varies per filter, see below
Filter(param: String, internalName: String)

// only on filters that explicitly support it: ExactFilter, PartialFilter, GreaterFilter, LessFilter, DateFilter
Filter(property: KProperty1<*, *>, param: String? = null)
```

### Public methods

| Method | Description |
|---|---|
| `filterByParam(query: Q, value: String)` | Abstract — apply the filter for one raw string value. |
| `filterByParam(query: Q, request: BasicRequest<*>)` | Looks up `param` in the request, applies default value/ignore rules, then delegates to the above. |
| `filterByParam(query: Q, body: Map<String, Any?>)` | Same, reading from a plain map instead of a request. |
| `addIgnore(vararg ignored: String): Filter<T, Q>` | Values that should be treated as "not provided". |
| `setDefaultValue(value: String?): Filter<T, Q>` | Value used when the parameter is missing. |
| `transform(value: String): T` | Converts the raw string to the filter's value type. Default: unchecked cast to `T` (most filters override this). |
| `supportBodyOperation(): Boolean` | Whether this filter may be used in a JSON body request. Defaults to `true`; no built-in filter overrides it to `false`. |

### Protected helpers (used by prefix-based filters)

| Method | Returns | Description |
|---|---|---|
| `getOperator(value: String)` | `BaradumOperator` | Reads a `<=`, `>=`, `<>`, `>`, or `<` prefix; defaults to `EQUAL`. |
| `cleanValue(value: String)` | `String` | Strips that same prefix off the value. |

---

## ExactFilter

Exact match with automatic type conversion.

```kotlin
ExactFilter(param: String, internalName: String = param)
ExactFilter(property: KProperty1<*, *>, param: String? = null)

// factory methods
ExactFilter.of(property: KProperty1<*, *>): ExactFilter
ExactFilter.of(property: KProperty1<*, *>, param: String): ExactFilter
```

**Type conversion** (in order): `"true"`/`"false"` → `Boolean`; digits, length < 10 → `Int`; longer digit strings → `Long`; `-?\d+\.\d+` → `Double`; a UUID-shaped string (`8-4-4-4-12` hex) → `java.util.UUID`; anything else → `String` unchanged.

```kotlin
ExactFilter("status")                    // ?status=ACTIVE
ExactFilter("userId", "user_id")         // ?userId=123 -> user_id = 123
ExactFilter(User::email, "userEmail")    // ?userEmail=test@example.com
```

> The UUID auto-detection is for columns that are actually typed as UUID. If your column is a plain `String`/`VARCHAR` that happens to hold UUID-formatted text, the QueryDSL backend coerces the value back to a string automatically before comparing.

---

## PartialFilter

`LIKE` filtering with a configurable wildcard strategy.

```kotlin
PartialFilter(param: String, internalName: String = param)
PartialFilter(property: KProperty1<*, *>, param: String? = null)

fun setStrategy(strategy: SearchLikeStrategy): PartialFilter

// factory methods
PartialFilter.of(property: KProperty1<*, *>): PartialFilter
PartialFilter.of(property: KProperty1<*, *>, param: String): PartialFilter
```

**Strategies** (`SearchLikeStrategy`): `FINAL` → `value%` (default), `START` → `%value`, `COMPLETE` → `%value%`. If the incoming value already contains `%`, the strategy is skipped and the value is used verbatim.

```kotlin
PartialFilter("username")                                       // ?username=john -> 'john%'
PartialFilter(User::email, "search").setStrategy(SearchLikeStrategy.COMPLETE) // -> '%john%'
```

---

## SearchFilter

OR search across several fields. **No Kotlin property-reference constructor.**

```kotlin
SearchFilter(param: String, vararg fields: String)

fun setInternalNames(names: List<String>): SearchFilter
fun setStrategy(strategy: SearchLikeStrategy): SearchFilter   // default: COMPLETE

// factory method — defaults param to "search"
SearchFilter.of(vararg fields: String): SearchFilter
```

```java
SearchFilter.of("name", "email", "phone")
// ?search=john -> name LIKE '%john%' OR email LIKE '%john%' OR phone LIKE '%john%'

new SearchFilter("q", "title", "description", "tags")
// ?q=kotlin -> title LIKE '%kotlin%' OR description LIKE '%kotlin%' OR tags LIKE '%kotlin%'
```

The default strategy here is `COMPLETE` (`%value%`), unlike `PartialFilter`'s default of `FINAL`.

---

## EnumFilter

Enum matching, single value or comma-separated `IN`. **No Kotlin property-reference constructor** — the enum `Class` always has to be passed explicitly, and Kotlin has no way to infer it from a property reference alone.

```kotlin
EnumFilter<E : Enum<E>, Q : QueryBuilder<*>>(
    param: String,
    internalName: String = param,
    enumClass: Class<E>
)
```

```java
EnumFilter("status", Status.class)
// ?status=ACTIVE            -> status = ACTIVE
// ?status=ACTIVE,PENDING    -> status IN (ACTIVE, PENDING)

EnumFilter("status", "order_status", OrderStatus.class)  // custom internal name
```

Throws `FilterException` (listing the allowed values) if the incoming value isn't a valid enum constant.

---

## IntervalFilter

Numeric range filtering. **No Kotlin property-reference constructor.**

```kotlin
IntervalFilter(param: String, internalName: String = param)
```

| Input | Result |
|---|---|
| `25` | `field = 25` |
| `18-65` | `field >= 18 AND field <= 65` |
| `18,65` | same as `18-65` (comma accepted for backward compatibility) |
| `18-` | `field >= 18` |
| `-65` | `field <= 65` |

---

## InFilter

`IN (...)` with a configurable delimiter. **No Kotlin property-reference constructor.**

```kotlin
InFilter(param: String, internalName: String = param, delimiter: String = ",")
```

The delimiter is a constructor parameter, fixed at creation — there is no `setDelimiter()` method.

```kotlin
InFilter("country")                        // ?country=US,CA,MX -> country IN ('US','CA','MX')
InFilter("ids", "ids", "|")                // ?ids=1|2|3        -> id IN (1,2,3)
InFilter("tags", delimiter = "|")          // same, named argument
```

Throws `FilterException` if the value list ends up empty (e.g. an empty string).

---

## IsNullFilter

`IS NULL` / `IS NOT NULL`. **No Kotlin property-reference constructor.**

```kotlin
IsNullFilter(param: String, internalName: String = param)
```

| Input (case-insensitive) | Result |
|---|---|
| `null`, `true`, `1`, `yes` | `IS NULL` |
| `not_null`, `false`, `0`, `no` | `IS NOT NULL` |
| anything else | throws `FilterException` |

---

## ComparisonFilter

One parameter, any comparison operator via a prefix. **No Kotlin property-reference constructor.**

```kotlin
ComparisonFilter(param: String, internalName: String = param)
```

| Prefix | Operator | Example |
|---|---|---|
| `>=` | `GREATER_OR_EQUAL` | `?price=>=50` |
| `<=` | `LESS_OR_EQUAL` | `?price=<=500` |
| `!=` | `DIFF` | `?price=!=0` |
| `>` | `GREATER` | `?price=>100` |
| `<` | `LESS` | `?price=<1000` |
| *(none)* | `EQUAL` | `?price=99.99` |

The resulting value is always passed through as a `String` (no numeric parsing) — the query backend/database handles the comparison. Throws `FilterException` if the value is empty after stripping the prefix.

---

## GreaterFilter

Strictly-greater or greater-or-equal, with best-effort numeric parsing.

```kotlin
GreaterFilter(param: String, internalName: String = param, orEqual: Boolean = false)
GreaterFilter(property: KProperty1<*, *>, param: String? = null, orEqual: Boolean = false)

// factory methods
GreaterFilter.of(property: KProperty1<*, *>): GreaterFilter
GreaterFilter.of(property: KProperty1<*, *>, orEqual: Boolean): GreaterFilter
GreaterFilter.of(property: KProperty1<*, *>, param: String, orEqual: Boolean): GreaterFilter
```

Value parsing: contains `.` → `Double`; parses as a `Long` outside `Int` range → `Long`; else → `Int`; falls back to the raw `String` if none of those parse. Throws `FilterException` on a blank value.

```kotlin
GreaterFilter("age")                              // ?age=18 -> age > 18
GreaterFilter("minAge", "age", true)              // ?minAge=18 -> age >= 18
GreaterFilter.of(User::age, orEqual = true)
```

---

## LessFilter

Mirror of `GreaterFilter` for the opposite direction.

```kotlin
LessFilter(param: String, internalName: String = param, orEqual: Boolean = false)
LessFilter(property: KProperty1<*, *>, param: String? = null, orEqual: Boolean = false)

LessFilter.of(property: KProperty1<*, *>): LessFilter
LessFilter.of(property: KProperty1<*, *>, orEqual: Boolean): LessFilter
LessFilter.of(property: KProperty1<*, *>, param: String, orEqual: Boolean): LessFilter
```

```kotlin
LessFilter("age")                                 // ?age=65 -> age < 65
LessFilter("maxAge", "age", true)                 // ?maxAge=65 -> age <= 65
LessFilter.of(User::age, orEqual = true)
```

---

## DateFilter

The most configurable filter: five date types, custom patterns, a builder, and Kotlin property references.

```kotlin
DateFilter(param: String, internalName: String = param, dateType: DateType = DateType.LOCAL_DATE, pattern: String? = null)
DateFilter(property: KProperty1<*, *>, param: String? = null, dateType: DateType = DateType.LOCAL_DATE, pattern: String? = null)
```

**`DateType`**: `LOCAL_DATE` (default, pattern `yyyy-MM-dd`), `LOCAL_DATE_TIME` (`yyyy-MM-dd'T'HH:mm:ss`), `UTIL_DATE` (`yyyy-MM-dd`), `SQL_DATE` (`yyyy-MM-dd`), `SQL_TIMESTAMP` (`yyyy-MM-dd HH:mm:ss`).

**Factory methods** — each takes `(param: String, pattern: String? = null, internalName: String = param)` or the property-reference overload `(property: KProperty1<*, *>, pattern: String? = null, param: String? = null)`:

```
DateFilter.forLocalDate(...)
DateFilter.forLocalDateTime(...)
DateFilter.forUtilDate(...)
DateFilter.forSqlDate(...)
DateFilter.forSqlTimestamp(...)
```

**Builder:**

```kotlin
DateFilter.builder(param: String): Builder
DateFilter.builder(property: KProperty1<*, *>): Builder
DateFilter.builder(property: KProperty1<*, *>, param: String): Builder

// Builder methods (all chainable, .build() returns the DateFilter)
.withInternalName(name: String)
.useLocalDate() / .useLocalDateTime() / .useUtilDate() / .useSqlDate() / .useSqlTimestamp()
.withPattern(pattern: String)
.build(): DateFilter
```

**Accepted value formats:**

| Format | Meaning | SQL |
|---|---|---|
| `2024-01-01` | exact | `date = '2024-01-01'` |
| `2024-01-01\|2024-12-31` | range | `date >= ... AND date <= ...` |
| `>`, `>=`, `<`, `<=`, `<>` prefix | comparison | `date > '2024-01-01'` etc. |

```kotlin
DateFilter("createdAt")                                            // LocalDate, ISO pattern
DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")
DateFilter.forUtilDate("birthDate", "MM-dd-yyyy")
DateFilter.builder("eventDate").useLocalDate().withPattern("yyyy/MM/dd").withInternalName("event_date").build()
DateFilter.forLocalDate(Event::createdAt)                          // Kotlin property reference
```

Throws `FilterException` (naming the expected pattern and date type) when the value can't be parsed.

---

## CustomFilter (Hefesto module)

`io.github.robertomike.baradum.hefesto.filters.CustomFilter` — lambda-based filtering, Hefesto backend only.

```kotlin
CustomFilter(param: String, consumer: BiConsumer<HefestoQueryBuilder<out BaseModel>, String>)
```

```java
new CustomFilter<>("status", (query, value) -> {
    if (value.equals("premium")) {
        query.where("subscription_level", BaradumOperator.GREATER, 5);
    } else {
        query.where("status", BaradumOperator.EQUAL, value);
    }
})
```

There's no core-module equivalent and no QueryDSL-specific one — see [Writing your own filter](#writing-your-own-filter) if you're on QueryDSL.

---

## Writing your own filter

Subclass `Filter<T, Q>` directly when nothing built-in fits:

```kotlin
class PriceRangeFilter<Q : QueryBuilder<*>>(
    param: String,
    internalName: String = param
) : Filter<Pair<Double, Double>, Q>(param, internalName) {

    override fun filterByParam(query: Q, value: String) {
        val (min, max) = parseRange(value)
        if (min > 0) query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, min)
        if (max > 0) query.where(internalName, BaradumOperator.LESS_OR_EQUAL, max)
    }

    override fun transform(value: String): Pair<Double, Double> = parseRange(value)

    private fun parseRange(value: String): Pair<Double, Double> {
        val parts = value.split("-")
        return (parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0) to (parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0)
    }
}
```

To also accept a Kotlin property reference, add a second constructor delegating to the base class's, the same way `ExactFilter`/`PartialFilter`/etc. do:

```kotlin
class PriceRangeFilter<Q : QueryBuilder<*>> : Filter<Pair<Double, Double>, Q> {
    constructor(param: String, internalName: String = param) : super(param, internalName)
    constructor(property: KProperty1<*, *>, param: String? = null) : super(property, param)
    // ...
}
```

## Errors

| Exception | Thrown when |
|---|---|
| `FilterException` | Invalid input for a filter (bad enum value, unparseable date, empty required value, disallowed body field). |
| `SortableException` | An unrecognized field is requested via `?sort=` or a body `sorts` entry. |
| `BaradumException` | No `QueryBuilderProvider` found for `Baradum.make(Class)`, or a malformed JSON body. |

All three extend `RuntimeException` and carry a human-readable `message`.
