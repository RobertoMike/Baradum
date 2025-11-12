# Baradum Filter API Reference

Complete reference guide for all available filters in Baradum.

## Table of Contents

- [Base Filter Class](#base-filter-class)
- [Core Filters](#core-filters)
- [Comparison Filters](#comparison-filters)
- [Date & Time Filters](#date--time-filters)
- [Advanced Filters](#advanced-filters)
- [Creating Custom Filters](#creating-custom-filters)

---

## Base Filter Class

### Filter<T, Q>

Base abstract class for all filters.

#### Constructors

```kotlin
// String-based constructor
Filter(param: String, internalName: String)

// Kotlin property reference constructor (type-safe)
Filter(property: KProperty1<*, *>, param: String? = null)
```

#### Key Methods

| Method | Description |
|--------|-------------|
| `filterByParam(query: Q, value: String)` | Apply filter to query with string value |
| `filterByParam(query: Q, request: BasicRequest<*>)` | Apply filter using request |
| `filterByParam(query: Q, body: Map<String, Any?>)` | Apply filter using body map |
| `setDefaultValue(value: String?)` | Set default value when parameter is missing |
| `addIgnore(vararg ignored: String)` | Values to ignore (filter won't be applied) |
| `transform(value: String): T` | Transform string value to target type |
| `supportBodyOperation(): Boolean` | Whether filter supports body filtering (default: true) |

#### Protected Helper Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getOperator(value: String)` | `BaradumOperator` | Extract operator from value (>, >=, <, <=, <>) |
| `cleanValue(value: String)` | `String` | Remove operator prefix from value |

---

## Core Filters

### ExactFilter

Exact value matching with automatic type conversion.

#### Constructors

```java
// String-based
ExactFilter(String param)
ExactFilter(String param, String internalName)

// Kotlin property reference
ExactFilter(KProperty1<*, *> property)
ExactFilter(KProperty1<*, *> property, String param)
```

#### Factory Methods

```kotlin
ExactFilter.of(property: KProperty1<*, *>)
ExactFilter.of(property: KProperty1<*, *>, param: String)
```

#### Type Conversion

| Input | Converted To | Example |
|-------|--------------|---------|
| `"true"` / `"false"` | Boolean | `isActive=true` |
| `"123"` | Integer | `age=25` |
| `"9999999999"` | Long | `id=9999999999` |
| `"99.99"` | Double | `price=99.99` |
| Other | String | `status=ACTIVE` |

#### Examples

```kotlin
// Java
ExactFilter("status")                    // ?status=ACTIVE
ExactFilter("userId", "user_id")         // ?userId=123 → user_id = 123

// Kotlin
ExactFilter(User::status)                // ?status=ACTIVE
ExactFilter(User::email, "userEmail")    // ?userEmail=test@example.com
```

---

### PartialFilter

LIKE filtering with configurable wildcard strategies.

#### Constructors

```java
PartialFilter(String param)
PartialFilter(String param, String internalName)
PartialFilter(KProperty1<*, *> property)
PartialFilter(KProperty1<*, *> property, String param)
```

#### Methods

```java
PartialFilter setStrategy(SearchLikeStrategy strategy)
```

#### Wildcard Strategies

| Strategy | Pattern | Example Input | SQL Result |
|----------|---------|---------------|------------|
| `FINAL` (default) | `value%` | `john` | `name LIKE 'john%'` |
| `INITIAL` | `%value` | `john` | `name LIKE '%john'` |
| `COMPLETE` | `%value%` | `john` | `name LIKE '%john%'` |

#### Examples

```kotlin
// Default FINAL strategy
PartialFilter("username")                            // ?username=john → 'john%'

// Custom strategy
PartialFilter("email")
    .setStrategy(SearchLikeStrategy.COMPLETE)        // ?email=john → '%john%'

// Kotlin
PartialFilter(User::username)
PartialFilter(User::email, "search")
    .setStrategy(SearchLikeStrategy.COMPLETE)

// User-provided wildcards (used as-is)
// ?username=%JOHN% → name LIKE '%JOHN%'
```

---

### SearchFilter

Multi-field OR search across multiple columns.

#### Constructors

```java
SearchFilter(String param, String... fields)
SearchFilter(String param, List<String> fields)
```

#### Factory Methods

```java
// Defaults param to "search"
SearchFilter.of(String... fields)
SearchFilter.of(List<String> fields)
```

#### Examples

```java
// Search across multiple fields
SearchFilter.of("name", "email", "phone")
// ?search=john → name LIKE '%john%' OR email LIKE '%john%' OR phone LIKE '%john%'

// Custom parameter name
new SearchFilter("q", "title", "description", "tags")
// ?q=kotlin → title LIKE '%kotlin%' OR description LIKE '%kotlin%' OR tags LIKE '%kotlin%'
```

---

### EnumFilter

Enum value filtering with single or multiple value support.

#### Constructors

```java
EnumFilter(String param, Class<E> enumClass)
EnumFilter(String param, String internalName, Class<E> enumClass)
EnumFilter(KProperty1<*, *> property, Class<E> enumClass)
```

#### Behavior

| Input | Operator | SQL Result |
|-------|----------|------------|
| Single value | `EQUAL` | `status = 'ACTIVE'` |
| Comma-separated | `IN` | `status IN ('ACTIVE', 'PENDING')` |

#### Examples

```java
// Single enum value
EnumFilter("status", Status.class)
// ?status=ACTIVE → status = 'ACTIVE'

// Multiple values
// ?status=ACTIVE,PENDING,INACTIVE → status IN ('ACTIVE', 'PENDING', 'INACTIVE')

// Kotlin
EnumFilter(User::status, Status::class.java)
EnumFilter(Order::status, "orderStatus", OrderStatus::class.java)
```

---

### IntervalFilter

Numeric range filtering.

#### Constructors

```java
IntervalFilter(String param)
IntervalFilter(String param, String internalName)
IntervalFilter(KProperty1<*, *> property)
```

#### Supported Formats

| Format | Description | SQL Result |
|--------|-------------|------------|
| `25` | Single value | `age = 25` |
| `18-65` | Range (hyphen) | `age >= 18 AND age <= 65` |
| `18,65` | Range (comma) | `age >= 18 AND age <= 65` |
| `18-` | Min only | `age >= 18` |
| `-65` | Max only | `age <= 65` |

#### Examples

```java
IntervalFilter("age")
// ?age=25        → age = 25
// ?age=18-65     → age >= 18 AND age <= 65
// ?age=18-       → age >= 18
// ?age=-65       → age <= 65

// Kotlin
IntervalFilter(Product::price)
// ?price=100-500 → price >= 100 AND price <= 500
```

---

### InFilter

IN operator with comma-separated values.

#### Constructors

```java
InFilter(String param)
InFilter(String param, String internalName)
InFilter(KProperty1<*, *> property)
InFilter(KProperty1<*, *> property, String param)
```

#### Methods

```java
InFilter setDelimiter(String delimiter)  // Default: ","
```

#### Examples

```java
InFilter("country")
// ?country=US,CA,MX → country IN ('US', 'CA', 'MX')

InFilter("ids")
// ?ids=1,2,3,4,5 → id IN (1, 2, 3, 4, 5)

// Custom delimiter
InFilter("tags").setDelimiter("|")
// ?tags=java|kotlin|spring → tags IN ('java', 'kotlin', 'spring')

// Kotlin
InFilter(User::country, "countries")
```

---

### IsNullFilter

NULL / NOT NULL checks.

#### Constructors

```java
IsNullFilter(String param)
IsNullFilter(String param, String internalName)
IsNullFilter(KProperty1<*, *> property)
```

#### Accepted Values

| Input Value | Operator | SQL Result |
|-------------|----------|------------|
| `"null"`, `"true"`, `"1"`, `"yes"` | `IS_NULL` | `field IS NULL` |
| `"not_null"`, `"false"`, `"0"`, `"no"` | `IS_NOT_NULL` | `field IS NOT NULL` |

#### Examples

```java
IsNullFilter("deletedAt")
// ?deletedAt=null     → deletedAt IS NULL
// ?deletedAt=not_null → deletedAt IS NOT NULL

// Kotlin
IsNullFilter(User::deletedAt)
// ?deletedAt=true  → deletedAt IS NULL
// ?deletedAt=false → deletedAt IS NOT NULL
```

---

## Comparison Filters

### ComparisonFilter

Flexible comparison with operator prefix support.

#### Constructors

```java
ComparisonFilter(String param)
ComparisonFilter(String param, String internalName)
ComparisonFilter(KProperty1<*, *> property)
```

#### Supported Operators

| Prefix | Operator | Example | SQL Result |
|--------|----------|---------|------------|
| `>` | GREATER | `>100` | `field > 100` |
| `>=` | GREATER_OR_EQUAL | `>=100` | `field >= 100` |
| `<` | LESS | `<100` | `field < 100` |
| `<=` | LESS_OR_EQUAL | `<=100` | `field <= 100` |
| `!=` | DIFF | `!=0` | `field != 0` |
| (none) | EQUAL | `100` | `field = 100` |

#### Examples

```java
ComparisonFilter("price")
// ?price=>100    → price > 100
// ?price=>=50    → price >= 50
// ?price=<1000   → price < 1000
// ?price=<=500   → price <= 500
// ?price=!=0     → price != 0
// ?price=99.99   → price = 99.99

// Kotlin
ComparisonFilter(Product::price, "minPrice")
```

---

### GreaterFilter

Greater than comparisons with optional equality.

#### Constructors

```java
GreaterFilter(String param)
GreaterFilter(String param, String internalName)
GreaterFilter(String param, String internalName, boolean orEqual)
GreaterFilter(KProperty1<*, *> property)
GreaterFilter(KProperty1<*, *> property, String param)
GreaterFilter(KProperty1<*, *> property, String param, boolean orEqual)
```

#### Factory Methods

```kotlin
GreaterFilter.of(property: KProperty1<*, *>)
GreaterFilter.of(property: KProperty1<*, *>, orEqual: Boolean)
GreaterFilter.of(property: KProperty1<*, *>, param: String, orEqual: Boolean)
```

#### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `orEqual` | Boolean | `false` | If true, uses `>=` instead of `>` |

#### Type Conversion

Automatically attempts to parse as:
1. Double (if contains `.`)
2. Long (if value > Int.MAX_VALUE)
3. Int
4. String (fallback)

#### Examples

```java
// Strictly greater than
GreaterFilter("age")
// ?age=18 → age > 18

// Greater than or equal
GreaterFilter("age", true)
// ?age=18 → age >= 18

// With custom param name
GreaterFilter("minAge", "age", true)
// ?minAge=18 → age >= 18

// Kotlin
GreaterFilter.of(User::age, orEqual = true)
GreaterFilter(Product::price, "minPrice", orEqual = true)
```

---

### LessFilter

Less than comparisons with optional equality.

#### Constructors

```java
LessFilter(String param)
LessFilter(String param, String internalName)
LessFilter(String param, String internalName, boolean orEqual)
LessFilter(KProperty1<*, *> property)
LessFilter(KProperty1<*, *> property, String param)
LessFilter(KProperty1<*, *> property, String param, boolean orEqual)
```

#### Factory Methods

```kotlin
LessFilter.of(property: KProperty1<*, *>)
LessFilter.of(property: KProperty1<*, *>, orEqual: Boolean)
LessFilter.of(property: KProperty1<*, *>, param: String, orEqual: Boolean)
```

#### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `orEqual` | Boolean | `false` | If true, uses `<=` instead of `<` |

#### Examples

```java
// Strictly less than
LessFilter("age")
// ?age=65 → age < 65

// Less than or equal
LessFilter("age", true)
// ?age=65 → age <= 65

// With custom param name
LessFilter("maxAge", "age", true)
// ?maxAge=65 → age <= 65

// Kotlin
LessFilter.of(User::age, orEqual = true)
LessFilter(Product::price, "maxPrice", orEqual = true)
```

---

## Date & Time Filters

### DateFilter

Comprehensive date filtering with multiple type support.

#### Supported Date Types

| Enum Value | Java Type | Default Pattern |
|------------|-----------|-----------------|
| `LOCAL_DATE` | `java.time.LocalDate` | `yyyy-MM-dd` |
| `LOCAL_DATE_TIME` | `java.time.LocalDateTime` | `yyyy-MM-dd'T'HH:mm:ss` |
| `UTIL_DATE` | `java.util.Date` | `yyyy-MM-dd` |
| `SQL_DATE` | `java.sql.Date` | `yyyy-MM-dd` |
| `SQL_TIMESTAMP` | `java.sql.Timestamp` | `yyyy-MM-dd HH:mm:ss` |

#### Constructors

```java
// Default (LocalDate)
DateFilter(String param)
DateFilter(String param, String internalName)

// With specific type and pattern
DateFilter(String param, String internalName, DateType dateType, String pattern)
```

#### Factory Methods

```java
// Type-specific factory methods
DateFilter.forLocalDate(String param, String pattern)
DateFilter.forLocalDateTime(String param, String pattern)
DateFilter.forUtilDate(String param, String pattern)
DateFilter.forSqlDate(String param, String pattern)
DateFilter.forSqlTimestamp(String param, String pattern)

// Builder pattern
DateFilter.builder(String param)
    .useLocalDate()
    .withPattern("yyyy/MM/dd")
    .build()
```

#### Builder Methods

| Method | Description |
|--------|-------------|
| `withInternalName(String name)` | Set custom internal field name |
| `useLocalDate()` | Use `java.time.LocalDate` |
| `useLocalDateTime()` | Use `java.time.LocalDateTime` |
| `useUtilDate()` | Use `java.util.Date` |
| `useSqlDate()` | Use `java.sql.Date` |
| `useSqlTimestamp()` | Use `java.sql.Timestamp` |
| `withPattern(String pattern)` | Set custom date pattern |
| `build()` | Build the DateFilter instance |

#### Supported Formats

| Format | Description | Example | SQL Result |
|--------|-------------|---------|------------|
| Single date | Exact match | `2024-01-01` | `date = '2024-01-01'` |
| Range (pipe) | Between dates | `2024-01-01\|2024-12-31` | `date >= '2024-01-01' AND date <= '2024-12-31'` |
| Greater than | After date | `>2024-01-01` | `date > '2024-01-01'` |
| Greater or equal | On or after | `>=2024-01-01` | `date >= '2024-01-01'` |
| Less than | Before date | `<2024-12-31` | `date < '2024-12-31'` |
| Less or equal | On or before | `<=2024-12-31` | `date <= '2024-12-31'` |
| Not equal | Exclude date | `<>2024-06-15` | `date != '2024-06-15'` |

#### Examples

```java
// Simple LocalDate
DateFilter("createdAt")
// ?createdAt=2024-01-01 → createdAt = '2024-01-01'

// LocalDateTime with custom pattern
DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")
// ?updatedAt=15/03/2024 14:30:00

// java.util.Date with custom format
DateFilter.forUtilDate("birthDate", "MM-dd-yyyy")
// ?birthDate=03-15-1990

// SQL Timestamp
DateFilter.forSqlTimestamp("eventTime", "yyyy-MM-dd HH:mm:ss")

// Builder pattern
DateFilter.builder("eventDate")
    .useLocalDate()
    .withPattern("yyyy/MM/dd")
    .withInternalName("event_date")
    .build()

// Range filtering
// ?createdAt=2024-01-01|2024-12-31
// → createdAt >= '2024-01-01' AND createdAt <= '2024-12-31'

// Comparison operators
// ?createdAt=>2024-01-01  → createdAt > '2024-01-01'
// ?createdAt=>=2024-01-01 → createdAt >= '2024-01-01'
```

---

## Advanced Filters

### CustomFilter

Lambda-based custom filtering logic.

#### Constructor

```java
CustomFilter(String param, BiConsumer<Q, String> filterFunction)
CustomFilter(String param, String internalName, BiConsumer<Q, String> filterFunction)
```

#### Examples

```java
// Simple custom logic
new CustomFilter<>("status", (query, value) -> {
    if (value.equals("premium")) {
        query.where("subscription_level", BaradumOperator.GREATER, 5);
    } else {
        query.where("status", BaradumOperator.EQUAL, value);
    }
})

// Complex custom filter
new CustomFilter<>("dateRange", (query, value) -> {
    String[] parts = value.split(",");
    LocalDate start = LocalDate.parse(parts[0]);
    LocalDate end = LocalDate.parse(parts[1]);
    
    query.where("created_at", BaradumOperator.GREATER_OR_EQUAL, start);
    query.where("created_at", BaradumOperator.LESS_OR_EQUAL, end);
    query.where("status", BaradumOperator.EQUAL, "ACTIVE");
})
```

---

## Creating Custom Filters

### Basic Custom Filter

```kotlin
class RatingFilter<Q : QueryBuilder<*>>(
    param: String,
    internalName: String = param
) : Filter<Double, Q>(param, internalName) {

    override fun filterByParam(query: Q, value: String) {
        val rating = value.toDouble()
        query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, rating)
    }

    override fun transform(value: String): Double {
        return value.toDouble()
    }
}
```

### Advanced Custom Filter with KProperty Support

```kotlin
class PriceRangeFilter<Q : QueryBuilder<*>> : Filter<Pair<Double, Double>, Q> {

    constructor(param: String, internalName: String = param) 
        : super(param, internalName)

    constructor(property: KProperty1<*, *>, param: String? = null) 
        : super(property, param)

    override fun filterByParam(query: Q, value: String) {
        val (min, max) = parseRange(value)
        
        if (min > 0) {
            query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, min)
        }
        if (max > 0) {
            query.where(internalName, BaradumOperator.LESS_OR_EQUAL, max)
        }
    }

    private fun parseRange(value: String): Pair<Double, Double> {
        val parts = value.split("-")
        val min = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val max = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        return min to max
    }

    override fun transform(value: String): Pair<Double, Double> {
        return parseRange(value)
    }
}
```

### Custom Filter with Configuration

```kotlin
class WeightedSearchFilter<Q : QueryBuilder<*>>(
    param: String,
    private val fieldWeights: Map<String, Int>
) : Filter<String, Q>(param, param) {

    override fun filterByParam(query: Q, value: String) {
        val searchTerm = "%$value%"
        
        // Sort fields by weight (highest first)
        val sortedFields = fieldWeights.entries
            .sortedByDescending { it.value }
        
        // Apply OR conditions for each field
        sortedFields.forEach { (field, _) ->
            query.orWhere(field, BaradumOperator.LIKE, searchTerm)
        }
    }
}

// Usage
WeightedSearchFilter("search", mapOf(
    "title" to 10,
    "description" to 5,
    "tags" to 3
))
```

---

## Filter Chaining & Combination

### Multiple Filters Example

```java
return Baradum.make(Product.class)
    .allowedFilters(
        ExactFilter("category"),
        PartialFilter("name"),
        EnumFilter("status", ProductStatus.class),
        GreaterFilter("price", "minPrice", true),
        LessFilter("price", "maxPrice", true),
        DateFilter.forLocalDate("releaseDate"),
        InFilter("brand"),
        IsNullFilter("deletedAt")
    )
    .allowedSort("price", "name", "releaseDate")
    .page();
```

**URL Example:**
```
GET /products?category=electronics&name=laptop&minPrice=500&maxPrice=2000
    &status=IN_STOCK&brand=Apple,Dell,HP&releaseDate=>2023-01-01
    &deletedAt=null&sort=-price
```

### Default Values

```java
ExactFilter("status")
    .setDefaultValue("ACTIVE")
// If ?status is not provided, uses "ACTIVE" as default
```

### Ignoring Values

```java
IntervalFilter("age")
    .addIgnore("0", "null", "")
// Filters out these values - won't apply filter if received
```

---

## Best Practices

1. **Use Kotlin Property References** for type safety when possible
2. **Set Default Values** for required filters
3. **Ignore Invalid Values** to prevent SQL errors
4. **Choose Appropriate Filter Types** - don't use PartialFilter for IDs
5. **Use Builder Patterns** for complex configurations
6. **Leverage Factory Methods** for cleaner code
7. **Document Custom Filters** with clear examples
8. **Test Edge Cases** - empty strings, special characters, etc.

---

## Error Handling

All filters throw `FilterException` for invalid input:

```java
try {
    DateFilter.forLocalDate("createdAt").filterByParam(query, "invalid-date");
} catch (FilterException e) {
    // Handle: "Invalid date format for 'invalid-date' in filter 'createdAt'"
}
```

Common exceptions:
- Invalid date format
- Invalid enum value
- Empty value for required filter
- Invalid numeric format

---

For more examples and integration guides, see [DOCUMENTATION.md](DOCUMENTATION.md)
