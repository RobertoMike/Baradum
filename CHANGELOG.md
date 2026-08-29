# Changelog

All notable changes to Baradum will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.1.0] - 2026-08-29

### Added

- **`NotInFilter`**: the mirror image of `InFilter`, emitting `NOT_IN` instead of `IN`. Same constructors, same configurable delimiter, Kotlin property-reference support included from the start.
- **`LIKE_IGNORE_CASE` operator** and a `setIgnoreCase(Boolean)` option on `PartialFilter` and `SearchFilter`, for case-insensitive matching. Implemented natively in QueryDSL (`Ops.LIKE_IC`); implemented in Hefesto via a raw `LOWER()`-wrapped Criteria API predicate, since Hefesto's own operator set has no case-insensitive `LIKE`.
- **`io.github.robertomike.baradum.core.filters.CustomFilter`**: a generic, backend-agnostic lambda filter (works with any `QueryBuilder<*>`), so QueryDSL users get the same convenience `baradum-hefesto`'s `CustomFilter` already offered, without hand-rolling a `Filter` subclass.
- **Kotlin property-reference constructors** added to `IntervalFilter`, `InFilter`, `IsNullFilter`, `ComparisonFilter`, and `EnumFilter` (enum class is still required explicitly on `EnumFilter` - it can't be inferred from a property reference). Every built-in filter except `SearchFilter` and `CustomFilter` now supports this.
- **`HefestoBaradum`**: `io.github.robertomike.baradum.hefesto.Baradum` renamed for consistency with `QueryDslBaradum` (the two same-named `Baradum` classes across modules were an easy source of confusion). Non-breaking - the old `Baradum` object remains as a `@Deprecated` compatibility shim delegating to `HefestoBaradum`.

### Fixed

- **`BaradumOperator.BETWEEN` was broken on both backends** when reached via a body-JSON filter request (`"operator": "BETWEEN"`): on Hefesto it silently fell back to `GREATER_OR_EQUAL`, dropping the upper bound and returning wrong data; on QueryDSL it always threw `IllegalArgumentException`, because `Filterable` only ever passed a single transformed value instead of the two-element list both backends expect. `Filterable` now splits `BETWEEN` values the same way it already did for `IN`/`NOT_IN`; Hefesto (which has no native `BETWEEN` operator) now builds it via a raw Criteria API `cb.between(...)` predicate instead of the broken fallback.
- `QueryDslQueryBuilder`: values that arrive as a non-`String` (e.g. a `UUID` auto-detected by `ExactFilter`) are now coerced back to `String` in the `StringPath` equality/inequality branches, avoiding a `ClassCastException` when a `String`/`VARCHAR` column happens to hold UUID-formatted text.
- `Baradum.make()`: fixed a missing space in the "no provider found" exception message, and simplified a redundant null/empty check.
- `IntervalFilter`: fixed range parsing for genuinely negative bounds (e.g. `"-10--5"`, previously misread as a positive `"max = 10"` bound) and for whitespace-padded ranges (e.g. `" 18 - 65 "`).
- `Page.hasNext`: now guards against `limit == 0` the same way `totalPages`/`currentPage` already did, so a zero-limit page no longer incorrectly reports `hasNext = true`.

### Deprecated

- `Baradum.request` (the static/shared request field): prefer `withParams(...)`/`withParam(...)` on the `Baradum` instance, which avoid shared mutable state. The field remains fully functional - `baradum-apache-tomcat`'s auto-configuration still uses it internally.
- `io.github.robertomike.baradum.hefesto.Baradum`: renamed to `HefestoBaradum` (see Added, above).

### Improved

- `ExactFilter`: the regex patterns used for type detection are now precompiled once instead of recompiled on every call.
- Stopped tracking `.idea/` IDE configuration in git.
- Added unit tests for `SearchFilter` (previously zero direct coverage) and a real-database integration test for the `BETWEEN`/`LIKE_IGNORE_CASE` Criteria API code on Hefesto.

### Documentation

- Rebuilt `README.md`, `DOCUMENTATION.md`, `FILTER_API_REFERENCE.md`, `QUICK_REFERENCE.md`, and `baradum-querydsl/README.md` for accuracy against the current codebase — corrected version numbers, removed references to files that don't exist (`MIGRATION_GUIDE.md`, `TEST_SUITE_SUMMARY.md`), corrected which filters actually support Kotlin property references, corrected the claimed Spring Boot 2/Tomcat 9 support (not present in the current `baradum-apache-tomcat` module), and fixed several non-compiling code examples. Updated again for the `3.1.0` additions above.
- Removed `ENHANCEMENT_SUMMARY.md` (a stale PR-summary document referencing a `README_NEW.md` that was never committed).

## [3.0.0] - 2025-11-10

### 🎉 Major Release - Modular Architecture & QueryDSL Support

Version 3.0.0 represents a significant evolution of Baradum with a complete architectural redesign, introducing a modular structure and new QueryDSL integration.

### 🏗️ Breaking Changes

#### Module Restructuring
- **Split into 4 independent modules**:
  - `baradum-core`: Core abstractions and filter definitions
  - `baradum-hefesto`: Hefesto/Hibernate implementation (previously the main module)
  - `baradum-querydsl`: NEW - QueryDSL integration module
  - `baradum-apache-tomcat`: Apache Tomcat request integration (Spring Boot 2 & 3)

#### Migration Required

**Old (2.x):**
```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum</artifactId>
    <version>2.1.1</version>
</dependency>
```

**New (3.0.0) - Choose your implementation:**
```xml
<!-- For Hefesto/Hibernate (legacy behavior) -->
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-hefesto</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- OR for QueryDSL (new) -->
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-querydsl</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- Spring Boot integration (optional) -->
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-apache-tomcat</artifactId>
    <version>3.0.0</version>
</dependency>
```

#### API Changes
- Core abstractions moved to `baradum-core` package
- `Baradum.make()` now uses ServiceLoader for automatic QueryBuilder discovery
- No breaking changes to filter API - existing filter code should work without modification

### ✨ New Features

#### QueryDSL Module
- **Full QueryDSL integration** with type-safe query API
- **Extension functions** for Q-classes: `QUser.user.baradum(entityManager)`
- **Direct QueryBuilder access**: `QUser.user.queryBuilder(entityManager)`
- **Global path caching** with 11.76x performance improvement
- **81 comprehensive tests** including integration and performance tests
- Support for all Baradum operators with QueryDSL predicates
- Thread-safe cache implementation with `ConcurrentHashMap`

Example:
```kotlin
// Extension function approach (recommended)
val users = QUser.user
    .baradum(entityManager)
    .allowedFilters(
        ExactFilter(User::name),
        GreaterFilter(User::age, orEqual = true)
    )
    .withParams(request.parameterMap)
    .get()

// Direct QueryBuilder approach
val builder = QUser.user.queryBuilder(entityManager)
builder.where("age", BaradumOperator.GREATER, 18)
val users = builder.get()
```

#### Performance Optimizations
- **Global static cache** for reflection-based path lookups in QueryDSL
- **322 queries/second** sustained throughput in benchmarks
- **Thread-safe** caching with atomic operations
- Cache persists across all QueryBuilder instances
- Composite cache keys prevent entity type collisions

#### Enhanced Testing
- **44 integration tests** for QueryDSL with real H2 database
- **7 performance tests** with detailed benchmarks
- **16 extension function tests**
- **100+ Hefesto integration tests** with real database queries
- All modules: **BUILD SUCCESSFUL** with comprehensive test coverage

### 🔧 Improvements

#### Architecture
- **Modular design** allows choosing between Hefesto and QueryDSL implementations
- **ServiceLoader pattern** for automatic QueryBuilder discovery
- **Clean separation** of concerns between core, implementation, and web layers
- **Independent versioning** for each module

#### Code Quality
- Kotlin 2.0.21 with improved type inference
- JDK 17 baseline (toolchain support)
- Comprehensive KDoc and Javadoc documentation
- Consistent code style across all modules

#### Documentation
- **New**: baradum-querydsl/README.md with complete QueryDSL guide
- **Updated**: Main README with modular architecture information
- **Enhanced**: FILTER_API_REFERENCE.md with latest examples
- **New**: Performance benchmarks documentation
- Migration guides for 2.x → 3.0.0 transition

### 📊 Module Details

#### baradum-core (3.0.0)
- Core abstractions: `Baradum`, `QueryBuilder`, `Filter`
- All filter types: ExactFilter, PartialFilter, SearchFilter, etc.
- No database dependencies - pure API definitions
- Kotlin property reference support

#### baradum-hefesto (3.0.0)
- Hefesto 3.0.0 integration
- HefestoQueryBuilder implementation
- Backward compatible with existing Hefesto-based code
- 100+ integration tests with H2 database

#### baradum-querydsl (3.0.0) - NEW
- QueryDSL 5.0.0 integration
- Jakarta Persistence API 3.1.0
- Extension functions for fluent API
- Global caching for optimal performance
- 88 tests (integration + unit + performance)

#### baradum-apache-tomcat (3.0.0)
- Apache Tomcat 10.1.11 (Jakarta)
- Spring Boot 3 support, wiring the Hefesto backend
- Request parameter parsing
- Auto-configuration support
- **Correction (2026-08-29):** this module never shipped Spring Boot 2 / Tomcat 9 support as originally stated here — there is no `AutoConfigurationSpring2` class in the codebase. Use `withParams(...)` or a custom `BasicRequest` for Spring Boot 2 or non-Spring frameworks.

### 🐛 Bug Fixes

- Fixed page() method to count total before pagination
- Fixed entityManager access pattern in Kotlin tests
- Corrected type inference issues in filter constructors
- Resolved thread-safety issues with static state

### 🔬 Testing

- **Total tests**: 200+ across all modules
- **Integration tests**: Full database scenario coverage
- **Performance tests**: Benchmarked cache impact
- **Build time**: Optimized to ~20-30 seconds
- **CI/CD**: GitHub Actions workflow validated

### 📈 Performance Metrics

QueryDSL module benchmarks:
- First query (cache population): 12,417 μs
- Subsequent queries (cache hit): 1,055 μs
- **Speedup**: 11.76x faster
- **Throughput**: 322 queries/second
- **Complex queries** (7 filters + 2 sorts): 16ms average

### 🔮 Upgrade Path

1. **Assess your current setup**: Identify if you're using Hefesto or need QueryDSL
2. **Update dependencies**: Choose `baradum-hefesto` or `baradum-querydsl`
3. **Update imports**: Core classes moved to `baradum-core` package
4. **Test thoroughly**: Run full test suite after migration
5. **Consider QueryDSL**: Evaluate new QueryDSL module for type-safe queries

### 📦 Maven Central

All modules are available on Maven Central:
- `io.github.robertomike:baradum-core:3.0.0`
- `io.github.robertomike:baradum-hefesto:3.0.0`
- `io.github.robertomike:baradum-querydsl:3.0.0`
- `io.github.robertomike:baradum-apache-tomcat:3.0.0`

### 🙏 Acknowledgments

Thanks to all contributors and users who provided feedback during the development of 3.0.0.

---

## [2.1.1] - 2024-XX-XX

### Added
- Enhanced DateFilter with multiple type support (LocalDate, LocalDateTime, java.util.Date)
- New GreaterFilter and LessFilter for comparison operations
- Kotlin property reference support for type-safe filtering
- Builder patterns for complex filter configurations
- Comprehensive documentation (DOCUMENTATION.md, FILTER_API_REFERENCE.md)

### Changed
- DateFilter now supports custom patterns per instance
- EnumFilter requires enum class in constructor
- Improved type inference for filter constructors

### Deprecated
- Static DateFilter configuration (use builder pattern instead)

---

## [2.0.3] - 2024-XX-XX

### Added
- Spring Boot 3 support with Jakarta Persistence

### Changed
- Updated to Jakarta namespace (jakarta.* instead of javax.*)

---

## [1.0.1] - 2024-XX-XX

### Added
- Spring Boot 2 support
- Apache Tomcat 9 integration
- Basic filter types: ExactFilter, PartialFilter, SearchFilter, IntervalFilter

### Changed
- Initial stable release

---

## Migration Guides

### From 2.x to 3.0.0

**Step 1: Update Dependencies**

Replace your `baradum` dependency with the appropriate module:

```gradle
// Old
implementation 'io.github.robertomike:baradum:2.1.1'

// New - Hefesto (maintains backward compatibility)
implementation 'io.github.robertomike:baradum-hefesto:3.0.0'

// OR New - QueryDSL (new implementation)
implementation 'io.github.robertomike:baradum-querydsl:3.0.0'
```

**Step 2: Update Imports (if needed)**

Most imports remain the same, but some core classes moved:

```java
// These imports stay the same
import io.github.robertomike.baradum.core.filters.*;
import io.github.robertomike.baradum.core.enums.*;

// Baradum class might need update depending on module
// Hefesto: io.github.robertomike.baradum.hefesto.Baradum
// QueryDSL: Use extension functions or QueryDslBaradum
```

**Step 3: Test Your Application**

Run your full test suite to ensure compatibility. The filter API is backward compatible, so most code should work without changes.

**Step 4: Consider QueryDSL**

If you're looking for better type safety and performance, consider migrating to the QueryDSL module:

```kotlin
// Before (Hefesto)
val users = Baradum.make(User.class)
    .allowedFilters(ExactFilter("name"))
    .get()

// After (QueryDSL with extension functions)
val users = QUser.user
    .baradum(entityManager)
    .allowedFilters(ExactFilter(User::name))
    .withParams(params)
    .get()
```

### Benefits of Upgrading to 3.0.0

- ✅ **Modular architecture** - Use only what you need
- ✅ **QueryDSL support** - Type-safe queries with 11x performance boost
- ✅ **Better performance** - Global caching and optimizations
- ✅ **Enhanced testing** - 200+ tests across all modules
- ✅ **Future-proof** - Clean architecture for future enhancements
- ✅ **Backward compatible** - Hefesto module maintains 2.x behavior

---

[3.1.0]: https://github.com/RobertoMike/Baradum/compare/3.0.0-all...master
[3.0.0]: https://github.com/RobertoMike/Baradum/compare/2.1.1-baradum...3.0.0-all
[2.1.1]: https://github.com/RobertoMike/Baradum/releases/tag/2.1.1-baradum
[2.0.3]: https://github.com/RobertoMike/Baradum/releases/tag/2.0.3-apache-tomcat
[1.0.1]: https://github.com/RobertoMike/Baradum/releases/tag/1.0.1-apache-tomcat

<!--
Note: this project's actual git tags never carry a "v" prefix (e.g. "3.0.0-all",
"2.1.1-baradum") and are suffixed per-module. The links above were fixed to match
real tags on 2026-08-29; [3.1.0] has no tag yet since that version hasn't been
released — update it to a real tag/compare once it is (see .github/workflows/maven-publish.yml
for the "<version>-all" tagging convention that triggers a release).
-->
