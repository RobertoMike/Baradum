# Baradum Quick Reference

One-page cheat sheet. For explanations, see [DOCUMENTATION.md](DOCUMENTATION.md); for exhaustive constructors, see [FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md).

## Installation

```kotlin
dependencies {
    implementation("io.github.robertomike:baradum-hefesto:3.0.1")     // Hefesto/Hibernate
    // or
    implementation("io.github.robertomike:baradum-querydsl:3.0.1")    // QueryDSL

    implementation("io.github.robertomike:baradum-apache-tomcat:3.0.1") // optional: Spring Boot 3 + Hefesto request wiring
}
```

## Filter cheat sheet

| Filter | Construct | URL | Result | KProperty? |
|---|---|---|---|---|
| Exact | `ExactFilter("id")` | `?id=123` | `id = 123` | ✅ |
| Partial | `PartialFilter("name")` | `?name=john` | `name LIKE 'john%'` | ✅ |
| Search | `SearchFilter.of("name","email")` | `?search=john` | `name LIKE '%j%' OR email LIKE '%j%'` | ❌ |
| Enum | `EnumFilter("status", Status.class)` | `?status=ACTIVE` | `status = 'ACTIVE'` | ✅ (enum class still required) |
| Interval | `IntervalFilter("age")` | `?age=18-65` | `age >= 18 AND age <= 65` | ✅ |
| In | `InFilter("country")` | `?country=US,CA` | `country IN ('US','CA')` | ✅ |
| NotIn | `NotInFilter("excluded", "country")` | `?excluded=US,CA` | `country NOT IN ('US','CA')` | ✅ |
| IsNull | `IsNullFilter("deletedAt")` | `?deletedAt=null` | `deletedAt IS NULL` | ✅ |
| Comparison | `ComparisonFilter("price")` | `?price=>100` | `price > 100` | ✅ |
| Greater | `GreaterFilter("age", true)` | `?age=18` | `age >= 18` | ✅ |
| Less | `LessFilter("age", true)` | `?age=65` | `age <= 65` | ✅ |
| Date | `DateFilter.forLocalDate("d")` | `?d=>2024-01-01` | `d > '2024-01-01'` | ✅ |
| Custom | `new CustomFilter<>("f", (q,v) -> ...)` | any | your lambda | ❌ |

KProperty = has a constructor taking a Kotlin property reference (`User::field`) instead of a string. Only `SearchFilter` and `CustomFilter` lack one.

`PartialFilter`/`SearchFilter` also take `.setIgnoreCase(true)` for case-insensitive matching.

## Common patterns

```kotlin
// Basic
Baradum.make(User::class.java).allowedFilters("id", "username").get()

// Several filters + sort
Baradum.make(Product::class.java)
    .allowedFilters(
        ExactFilter(Product::category),
        PartialFilter(Product::name, "search"),
        GreaterFilter(Product::price, "minPrice", orEqual = true),
        LessFilter(Product::price, "maxPrice", orEqual = true),
        InFilter("brand"),
    )
    .allowedSort(Product::price, Product::name)
    .page(20)
```

```
GET /products?search=laptop&category=electronics&minPrice=500&maxPrice=2000&brand=Apple,Dell&sort=-price
```

## URL syntax

**Sorting:** `?sort=name` (ASC) · `?sort=-name` (DESC) · `?sort=name,-age` (multiple)

**Intervals:** `?age=25` (exact) · `?age=18-65` (range) · `?age=18-` (min only) · `?age=-65` (max only)

**Enum / In:** `?status=ACTIVE` (single) · `?status=ACTIVE,PENDING` (IN)

**Dates:** `?date=2024-01-01` (exact) · `?date=2024-01-01|2024-12-31` (range) · `?date=>2024-01-01` (after) · `?date=<=2024-12-31` (before or on)

**Null checks:** `?deleted=null` / `not_null` (also accepts `true`/`false`, `1`/`0`, `yes`/`no`)

## Configuration one-liners

```kotlin
ExactFilter("status").setDefaultValue("ACTIVE")          // fallback when param missing
IntervalFilter("age").addIgnore("0", "null", "")         // skip filter for these values
baradum.withParams(request.parameterMap.mapValues { it.value.first() })  // explicit param wiring
baradum.useBody()                                        // read filters from a JSON POST body instead
```

## Common mistakes

| Mistake | Fix |
|---|---|
| `InFilter("tags").setDelimiter("|")` | There's no `setDelimiter()` — pass it in the constructor: `InFilter("tags", delimiter = "|")`. |
| `SearchFilter(User::name)` | `SearchFilter` has no property-reference constructor (it takes multiple field names via vararg) — use `SearchFilter.of("name", ...)`. |
| `.page()` with no arguments | `page(limit, offset)` is required — use `.page(20)` (offset defaults to 0) or `.get()` for everything. |
| Expecting Spring Boot 2 / Tomcat 9 auto-config | `baradum-apache-tomcat` only wires Spring Boot 3 (Jakarta) + Hefesto today. |

## More

- **[DOCUMENTATION.md](DOCUMENTATION.md)** — full guide
- **[FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md)** — every constructor and edge case
- **[baradum-querydsl/README.md](baradum-querydsl/README.md)** — QueryDSL setup
- **[GitHub Issues](https://github.com/RobertoMike/Baradum/issues)** — report a bug or ask a question
