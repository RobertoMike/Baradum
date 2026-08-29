# Baradum

[![Maven Central](https://img.shields.io/maven-central/v/io.github.robertomike/baradum-core.svg)](https://search.maven.org/search?q=g:io.github.robertomike%20AND%20a:baradum*)
[![License](https://img.shields.io/github/license/RobertoMike/Baradum)](LICENSE.txt)

Baradum turns URL query parameters (or a JSON body) into safe, typed database filters and sorting — without hand-rolling a stack of `if (param != null)` branches for every endpoint.

```kotlin
// GET /users?status=ACTIVE&age=18-65&sort=-createdAt
Baradum.make(User::class.java)
    .allowedFilters(
        ExactFilter(User::status),
        IntervalFilter("age"),
    )
    .allowedSort(User::createdAt)
    .get()
```

You declare which filters and sort fields a query is allowed to use; Baradum reads the incoming request and builds the query for you.

## Modules

Baradum ships as four independent artifacts so you only pull in what you need:

| Module | Artifact | What it does | Depends on |
|---|---|---|---|
| **Core** | `baradum-core` | Filter/sort definitions, no database code. | — |
| **Hefesto** | `baradum-hefesto` | Executes queries via [HefestoSQL](https://github.com/RobertoMike/Hefesto) (Hibernate). | `baradum-core` |
| **QueryDSL** | `baradum-querydsl` | Executes queries via QueryDSL 5.0+ / Jakarta Persistence, using generated Q-classes. | `baradum-core` |
| **Apache Tomcat** | `baradum-apache-tomcat` | Wires the current `HttpServletRequest` into Baradum automatically in a Spring Boot 3 (Jakarta) app. | `baradum-hefesto` |

Pick **one** query backend (Hefesto or QueryDSL) and, optionally, the Apache Tomcat module if you're on Spring Boot and want request wiring for free — see [Choosing a backend](DOCUMENTATION.md#choosing-a-backend) for the trade-offs.

## Installation

Current version: **3.0.1**

```kotlin
dependencies {
    // Pick ONE query backend:
    implementation("io.github.robertomike:baradum-hefesto:3.0.1")
    // or
    implementation("io.github.robertomike:baradum-querydsl:3.0.1")

    // Optional: Spring Boot 3 request auto-wiring (requires baradum-hefesto)
    implementation("io.github.robertomike:baradum-apache-tomcat:3.0.1")
}
```

```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-hefesto</artifactId>
    <version>3.0.1</version>
</dependency>
```

> **Spring Boot support:** `baradum-apache-tomcat` targets Apache Tomcat 10 / Jakarta Servlet (Spring Boot 3) only, and only wires up the Hefesto backend. If you're on QueryDSL, or need Spring Boot 2 / Tomcat 9, wire the request in manually — see [Configuration](DOCUMENTATION.md#configuration).

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
    if (categoryId != null && minAge != null) {
        return repository.findByCategoryIdAndAgeGreaterThanEqual(categoryId, minAge);
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
        .get()
```

- `?categoryId=2` → `categoryId = 2`
- `?age=18-65` → `age >= 18 AND age <= 65`
- `?categoryId=2&age=18-65` → both applied

`.get()` returns a `List<T>`; use `.page(limit, offset)` for pagination or `.findFirst()` for a single `Optional<T>` result — see [Pagination](DOCUMENTATION.md#pagination-and-results).

## Filter catalogue

| Filter | Example URL | Result | Kotlin property support |
|---|---|---|---|
| `ExactFilter` | `?status=ACTIVE` | `status = 'ACTIVE'` | ✅ |
| `PartialFilter` | `?name=john` | `name LIKE 'john%'` | ✅ |
| `SearchFilter` | `?search=john` | `name LIKE '%john%' OR email LIKE '%john%'` | ❌ |
| `EnumFilter` | `?status=ACTIVE,PENDING` | `status IN ('ACTIVE','PENDING')` | ✅ |
| `IntervalFilter` | `?age=18-65` | `age >= 18 AND age <= 65` | ✅ |
| `InFilter` | `?country=US,CA,MX` | `country IN ('US','CA','MX')` | ✅ |
| `NotInFilter` | `?excluded=US,CA` | `country NOT IN ('US','CA')` | ✅ |
| `IsNullFilter` | `?deletedAt=null` | `deletedAt IS NULL` | ✅ |
| `ComparisonFilter` | `?price=>100` | `price > 100` | ✅ |
| `GreaterFilter` | `?age=18` | `age > 18` (or `>=` with `orEqual=true`) | ✅ |
| `LessFilter` | `?age=65` | `age < 65` (or `<=` with `orEqual=true`) | ✅ |
| `DateFilter` | `?date=>2024-01-01` | `date > '2024-01-01'` | ✅ |
| `CustomFilter` | any | your own lambda | ❌ |

"Kotlin property support" means the filter has a constructor that takes a property reference directly, e.g. `ExactFilter(User::status)`, for compile-time-checked field names. Only `SearchFilter` (multiple field names via vararg) and `CustomFilter` (always a plain param name) lack one. `PartialFilter` and `SearchFilter` also support `.setIgnoreCase(true)` for case-insensitive matching.

Full constructors, options, and edge cases for every filter: **[FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md)**.

## Sorting

```kotlin
Baradum.make(User::class.java)
    .allowedSort(User::name, User::createdAt) // or .allowedSort("name", "createdAt")
    .get()
```

- `?sort=name` → `ORDER BY name ASC`
- `?sort=-name` → `ORDER BY name DESC`
- `?sort=name,-createdAt` → both, in order

## Body-based filtering

For requests too complex for query strings, POST a JSON body instead — see [Body-based filtering](DOCUMENTATION.md#body-based-filtering) for the full schema and how to enable it with `.useBody()`.

## Documentation

| Doc | What's in it |
|---|---|
| **[DOCUMENTATION.md](DOCUMENTATION.md)** | Full guide: installation, choosing a backend, every filter with examples, sorting, body filtering, custom filters, configuration/wiring. |
| **[FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md)** | Exhaustive per-filter API reference: constructors, factory methods, accepted input formats. |
| **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** | One-page cheat sheet. |
| **[baradum-querydsl/README.md](baradum-querydsl/README.md)** | QueryDSL-specific setup, Q-classes, and extension functions. |
| **[CHANGELOG.md](CHANGELOG.md)** | Release history and migration notes. |

## Known limitations

- No automatic Swagger/OpenAPI generation — document your filter parameters manually.
- `baradum-apache-tomcat` only auto-wires the Hefesto backend on Spring Boot 3 (Jakarta); QueryDSL users and Spring Boot 2 / Tomcat 9 users must wire the request manually (see [Configuration](DOCUMENTATION.md#configuration)).

## Contributing

Contributions are welcome — feel free to open a Pull Request.

## License

MIT — see [LICENSE.txt](LICENSE.txt).

## Support

If Baradum saves you time, consider [buying me a coffee](https://www.buymeacoffee.com/robertomike).

[![Buy Me A Coffee](./buy-me-coffee.png)](https://www.buymeacoffee.com/robertomike)

---

**Made with ❤️ by Roberto Mike**
