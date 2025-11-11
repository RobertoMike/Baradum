# Baradum - Dynamic Filtering and Sorting for Java/Kotlin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.robertomike/baradum.svg)](https://search.maven.org/artifact/io.github.robertomike/baradum)
[![License](https://img.shields.io/github/license/RobertoMike/Baradum)](LICENSE.txt)

Baradum is a powerful library that simplifies filtering and sorting in your Java/Kotlin applications. It allows you to dynamically filter and sort database queries using URL parameters or request body, eliminating the need for complex conditional logic.

## 🚀 Key Features

- **Dynamic Filtering**: Filter data using URL parameters or JSON body without writing repetitive if-else conditions
- **Type-Safe Kotlin API**: Use Kotlin property references (`User::name`) for compile-time safety
- **Extensive Filter Types**: 15+ built-in filters for common use cases
- **Flexible Date Handling**: Support for LocalDate, LocalDateTime, java.util.Date, and SQL date types
- **Custom Patterns**: Configure date patterns and filter behaviors per instance
- **Smart Type Conversion**: Automatic conversion of string values to appropriate types
- **Spring Boot Integration**: Zero-configuration setup for Spring Boot 2 & 3
- **Builder Patterns**: Fluent APIs for complex filter configurations

## 📋 Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Filter Types Reference](#filter-types-reference)
- [Kotlin Property References (Type-Safe API)](#kotlin-property-references-type-safe-api)
- [Date Filtering](#date-filtering)
- [Comparison Filters](#comparison-filters)
- [Sorting](#sorting)
- [Body-Based Filtering](#body-based-filtering)
- [Custom Filters](#custom-filters)
- [Configuration](#configuration)
- [Advanced Examples](#advanced-examples)
- [Migration Guide](#migration-guide)

## 📦 Installation

### Maven

```xml
<!-- Hefesto (Hibernate/HefestoSQL) -->
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-hefesto</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- OR QueryDSL (Type-safe queries) -->
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum-querydsl</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    // Hefesto (Hibernate/HefestoSQL)
    implementation 'io.github.robertomike:baradum-hefesto:3.0.0'
    
    // OR QueryDSL (Type-safe queries)
    implementation 'io.github.robertomike:baradum-querydsl:3.0.0'
}
```

### Spring Boot Integration

For Spring Boot 3 with Apache Tomcat:

```gradle
dependencies {
    // Spring Boot 3 (Jakarta)
    implementation 'io.github.robertomike:baradum-apache-tomcat:3.0.0'
}
```

## 🎯 Quick Start

### Basic Example

**Without Baradum:**
```java
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) Integer minAge,
    @RequestParam(required = false) Integer maxAge
) {
    if (categoryId != null && minAge != null && maxAge != null) {
        return repository.findByCategoryIdAndAgeGreaterThanEqualAndAgeLessThanEqual(
            categoryId, minAge, maxAge
        );
    }
    // ... 6 more conditional branches
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

### Kotlin with Property References

```kotlin
@GetMapping("/users")
fun getUsers(): List<User> {
    return Baradum.make(User::class.java)
        .allowedFilters(ExactFilter(User::categoryId))
        .allowedFilters(IntervalFilter(User::age))
        .get()
}
```

**URL Examples:**
- `?categoryId=2` → Filters by `categoryId = 2`
- `?age=18-65` → Filters by `age >= 18 AND age <= 65`
- `?categoryId=2&age=18-65` → Both filters applied

## 📖 Filter Types Reference

### Core Filters

#### ExactFilter
Exact value matching with automatic type conversion.

```java
// Traditional
ExactFilter("status")

// Kotlin property reference
ExactFilter(User::status)
ExactFilter(User::email, "userEmail")  // Custom param name
```

**Examples:**
- `?status=ACTIVE` → `status = 'ACTIVE'`
- `?isActive=true` → `isActive = true` (boolean conversion)
- `?age=25` → `age = 25` (numeric conversion)

#### PartialFilter
LIKE filtering with wildcard strategies.

```java
// Default FINAL strategy (value%)
PartialFilter("username")

// Custom strategy
PartialFilter("email").setStrategy(SearchLikeStrategy.COMPLETE)

// Kotlin
PartialFilter(User::username)
PartialFilter(User::email, "search").setStrategy(SearchLikeStrategy.COMPLETE)
```

**Strategies:**
- `FINAL`: `value%` (default)
- `INITIAL`: `%value`
- `COMPLETE`: `%value%`

**Examples:**
- `?username=john` → `username LIKE 'john%'`
- User-provided wildcards: `?username=%JOHN%` → Used as-is

#### SearchFilter
Multi-field OR search.

```java
// Traditional
SearchFilter.of("name", "email", "phone")

// Kotlin
SearchFilter.of(User::name, User::email, User::phone)
```

**Example:**
- `?search=john` → `name LIKE '%john%' OR email LIKE '%john%' OR phone LIKE '%john%'`

#### EnumFilter
Enum value filtering with IN operator support.

```java
// Single value
EnumFilter("status", Status.class)

// Kotlin
EnumFilter(User::status, Status::class.java)
```

**Examples:**
- `?status=ACTIVE` → `status = 'ACTIVE'`
- `?status=ACTIVE,PENDING` → `status IN ('ACTIVE', 'PENDING')`

#### IntervalFilter
Numeric range filtering.

```java
IntervalFilter("age")
IntervalFilter(User::age)
```

**Examples:**
- `?age=25` → `age = 25`
- `?age=18-65` → `age >= 18 AND age <= 65`
- `?age=18,65` → Same as above (backward compatibility)
- `?age=18-` → `age >= 18`
- `?age=-65` → `age <= 65`

#### ComparisonFilter
Supports comparison operators with prefix notation.

```java
ComparisonFilter("price")
ComparisonFilter(User::salary)
```

**Examples:**
- `?price=>100` → `price > 100`
- `?price=>=50` → `price >= 50`
- `?price=<1000` → `price < 1000`
- `?price=<=500` → `price <= 500`
- `?price=!=0` → `price != 0`

#### InFilter
IN operator with comma-separated values.

```java
InFilter("country")
InFilter(User::country, "countries")
```

**Example:**
- `?country=US,CA,MX` → `country IN ('US', 'CA', 'MX')`

#### IsNullFilter
NULL/NOT NULL checks.

```java
IsNullFilter("deletedAt")
IsNullFilter(User::deletedAt)
```

**Examples:**
- `?deletedAt=null` → `deletedAt IS NULL`
- `?deletedAt=not_null` → `deletedAt IS NOT NULL`
- Accepts: `true/false`, `1/0`, `yes/no`, `null/not_null`

### Comparison Filters

#### GreaterFilter
Greater than comparisons.

```java
// Strictly greater than (>)
GreaterFilter("age")
GreaterFilter(User::age)

// Greater than or equal (>=)
GreaterFilter("age", true)
GreaterFilter(User::age, "minAge", true)

// Kotlin
GreaterFilter.of(User::age, orEqual = true)
```

**Examples:**
- `?age=18` with `orEqual=false` → `age > 18`
- `?age=18` with `orEqual=true` → `age >= 18`

#### LessFilter
Less than comparisons.

```java
// Strictly less than (<)
LessFilter("age")
LessFilter(User::age)

// Less than or equal (<=)
LessFilter("age", true)
LessFilter(User::age, "maxAge", true)

// Kotlin
LessFilter.of(User::age, orEqual = true)
```

**Examples:**
- `?age=65` with `orEqual=false` → `age < 65`
- `?age=65` with `orEqual=true` → `age <= 65`

## 🔍 Date Filtering

### DateFilter with Configurable Types

DateFilter now supports multiple date types and custom patterns:

```java
// Simple LocalDate (default)
DateFilter("createdAt")

// LocalDateTime with custom pattern
DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")

// java.util.Date
DateFilter.forUtilDate("birthDate", "MM-dd-yyyy")

// SQL Date
DateFilter.forSqlDate("startDate")

// SQL Timestamp
DateFilter.forSqlTimestamp("eventTime", "yyyy-MM-dd HH:mm:ss")

// Builder pattern
DateFilter.builder("eventDate")
    .useLocalDate()
    .withPattern("yyyy/MM/dd")
    .build()

// Kotlin with property references
DateFilter.forLocalDate(Event::createdAt)
DateFilter.forUtilDate(User::birthDate, "MM-dd-yyyy")
```

### Supported Date Types

| Type | Description | Default Pattern |
|------|-------------|-----------------|
| `LOCAL_DATE` | java.time.LocalDate | yyyy-MM-dd |
| `LOCAL_DATE_TIME` | java.time.LocalDateTime | yyyy-MM-dd'T'HH:mm:ss |
| `UTIL_DATE` | java.util.Date | yyyy-MM-dd |
| `SQL_DATE` | java.sql.Date | yyyy-MM-dd |
| `SQL_TIMESTAMP` | java.sql.Timestamp | yyyy-MM-dd HH:mm:ss |

### Date Filtering Examples

```java
DateFilter("createdAt")
```

**Range with pipe:**
- `?createdAt=2024-01-01|2024-12-31` → `createdAt >= '2024-01-01' AND createdAt <= '2024-12-31'`

**Comparison operators:**
- `?createdAt=>2024-01-01` → `createdAt > '2024-01-01'`
- `?createdAt=>=2024-01-01` → `createdAt >= '2024-01-01'`
- `?createdAt=<2024-12-31` → `createdAt < '2024-12-31'`
- `?createdAt=<=2024-12-31` → `createdAt <= '2024-12-31'`
- `?createdAt=<>2024-06-15` → `createdAt != '2024-06-15'`

**Single date:**
- `?createdAt=2024-01-01` → `createdAt = '2024-01-01'`

## 🎭 Kotlin Property References (Type-Safe API)

All filters support Kotlin property references for compile-time safety:

### Base Constructor

```kotlin
// Property reference uses property name for both param and internalName
Filter(User::name)

// Custom param name, property name for internal field
Filter(User::email, "searchEmail")
```

### Complete Example

```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val status: Status,
    val createdAt: LocalDateTime
)

@GetMapping("/users")
fun getUsers(): List<User> {
    return Baradum.make(User::class.java)
        .allowedFilters(
            ExactFilter(User::id),
            PartialFilter(User::name, "search"),
            PartialFilter(User::email, "search"),
            EnumFilter(User::status, Status::class.java),
            IntervalFilter(User::age),
            DateFilter.forLocalDateTime(User::createdAt),
            GreaterFilter(User::age, "minAge", orEqual = true),
            LessFilter(User::age, "maxAge", orEqual = true)
        )
        .allowedSort(User::name, User::createdAt)
        .page()
}
```

### Factory Methods

```kotlin
// All filters provide factory methods
ExactFilter.of(User::status)
PartialFilter.of(User::name)
GreaterFilter.of(User::age, orEqual = true)
LessFilter.of(User::age, orEqual = true)
```

## 📊 Sorting

### URL Parameter Sorting

```java
return Baradum.make(User.class)
    .allowedSort("name", "createdAt")
    .allowedSort(new OrderBy("alias", "field_name"))
    .get();
```

**URL Examples:**
- `?sort=name` → `ORDER BY name ASC`
- `?sort=-name` → `ORDER BY name DESC`
- `?sort=name,-createdAt` → `ORDER BY name ASC, createdAt DESC`

### Kotlin Property References for Sorting

```kotlin
Baradum.make(User::class.java)
    .allowedSort(User::name, User::createdAt)
    .get()
```

## 📝 Body-Based Filtering

For complex filtering scenarios, use JSON body:

```json
{
    "filters": [
        {
            "field": "id",
            "value": "1",
            "operator": "EQUAL"
        },
        {
            "field": "name",
            "value": "abc%",
            "operator": "LIKE",
            "type": "OR"
        },
        {
            "field": "status",
            "operator": "IS_NULL",
            "type": "AND"
        },
        {
            "subFilters": [
                {
                    "field": "id",
                    "value": "1",
                    "operator": "EQUAL"
                },
                {
                    "field": "status",
                    "value": "ACTIVE,INACTIVE",
                    "operator": "IN",
                    "type": "OR"
                }
            ]
        }
    ],
    "sorts": [
        {
            "field": "id"
        },
        {
            "field": "name",
            "sort": "DESC"
        }
    ]
}
```

### Supported Body Filters

- `ExactFilter`
- `EnumFilter`
- `DateFilter`
- All filters with `supportBodyOperation()` returning `true`

## 🔧 Custom Filters

### Creating Custom Filters

```kotlin
class CustomRangeFilter<Q : QueryBuilder<*>>(
    param: String,
    internalName: String = param
) : Filter<String, Q>(param, internalName) {

    override fun filterByParam(query: Q, value: String) {
        val parts = value.split("-")
        val min = parts[0].toInt()
        val max = parts[1].toInt()
        
        query.where(internalName, BaradumOperator.GREATER_OR_EQUAL, min)
        query.where(internalName, BaradumOperator.LESS_OR_EQUAL, max)
    }
}
```

### Lambda-Based Custom Filter

```java
return Baradum.make(User.class)
    .allowedFilters(
        new CustomFilter<>("status", (query, value) -> {
            if (value.equals("premium")) {
                query.where("subscription_level", BaradumOperator.GREATER, 5);
            } else {
                query.where("status", BaradumOperator.EQUAL, value);
            }
        })
    )
    .get();
```

## ⚙️ Configuration

### Spring Boot (Auto-Configuration)

No configuration needed! Spring Boot 2 & 3 are auto-configured.

### Manual Configuration

```java
public class BaradumConfig {
    @Bean
    public void configureBaradum(HttpServletRequest request) {
        // For Apache Tomcat 9 (Spring Boot 2)
        new AutoConfigurationSpring2(request);
        
        // For Apache Tomcat 10 (Spring Boot 3)
        new AutoConfigurationSpring3(request);
    }
}
```

### Custom Request Implementation

```java
public class MyCustomRequest extends BasicRequest<HttpServletRequest> {
    public MyCustomRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String findParamByName(String name) {
        return getRequest().getParameter(name);
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return getRequest().getReader();
    }
}

// Configure Baradum
Baradum.setRequest(new MyCustomRequest(request));
```

## 🎓 Advanced Examples

### E-commerce Product Filtering

```kotlin
@GetMapping("/products")
fun getProducts(): Page<Product> {
    return Baradum.make(Product::class.java)
        .allowedFilters(
            ExactFilter(Product::category),
            PartialFilter(Product::name, "search"),
            GreaterFilter(Product::price, "minPrice", orEqual = true),
            LessFilter(Product::price, "maxPrice", orEqual = true),
            EnumFilter(Product::status, ProductStatus::class.java),
            DateFilter.forLocalDate(Product::releaseDate),
            InFilter(Product::brand, "brands")
        )
        .allowedSort(Product::price, Product::name, Product::releaseDate)
        .page()
}
```

**URL Examples:**
- `?category=electronics&minPrice=100&maxPrice=500&search=laptop`
- `?brands=Apple,Samsung,Sony&status=IN_STOCK&sort=price`
- `?releaseDate=>2024-01-01&sort=-releaseDate`

### User Management with Complex Filtering

```java
@GetMapping("/users")
public List<User> getUsers() {
    return Baradum.make(User.class)
        .allowedFilters(
            PartialFilter("username"),
            PartialFilter("email"),
            SearchFilter.of("search", "username", "email", "fullName"),
            EnumFilter("role", Role.class),
            EnumFilter("status", UserStatus.class),
            DateFilter.forLocalDateTime("createdAt"),
            DateFilter.forLocalDateTime("lastLogin"),
            GreaterFilter("loginCount", "minLogins", true),
            IntervalFilter("age"),
            IsNullFilter("deletedAt")
        )
        .allowedSort("username", "createdAt", "lastLogin")
        .get();
}
```

### Event Calendar Filtering

```kotlin
@GetMapping("/events")
fun getEvents(): List<Event> {
    return Baradum.make(Event::class.java)
        .allowedFilters(
            ExactFilter(Event::organizerId),
            PartialFilter(Event::title, "search"),
            EnumFilter(Event::category, EventCategory::class.java),
            DateFilter.builder<QueryBuilder<*>>("eventDate")
                .useLocalDateTime()
                .withPattern("yyyy-MM-dd HH:mm")
                .build(),
            GreaterFilter(Event::attendeeCount, "minAttendees", true),
            LessFilter(Event::attendeeCount, "maxAttendees", true),
            InFilter(Event::location, "locations")
        )
        .allowedSort(Event::eventDate, Event::title)
        .get()
}
```

## 🔄 Migration Guide

### Migrating from 2.0.x to 2.1.x

#### DateFilter Changes

**Before:**
```java
DateFilter.setDateFormat("dd/MM/yyyy");  // Global static configuration
DateFilter("createdAt")
```

**After:**
```java
// Per-instance configuration (recommended)
DateFilter.forUtilDate("createdAt", "dd/MM/yyyy")

// Or builder pattern
DateFilter.builder("createdAt")
    .useUtilDate()
    .withPattern("dd/MM/yyyy")
    .build()
```

#### EnumFilter Changes

**Before:**
```java
EnumFilter("status")
    .setEnumClass(Status.class)
```

**After:**
```java
// Enum class is now required in constructor
EnumFilter("status", Status.class)
```

#### New Filters Available

Add these new filters to your arsenal:

```java
// Greater/Less comparisons
GreaterFilter("age", true)  // >= operator
LessFilter("maxAge", true)  // <= operator

// ComparisonFilter for flexible operators
ComparisonFilter("price")  // Supports >, >=, <, <=, !=
```

#### Kotlin Property References

**Before:**
```kotlin
ExactFilter("username")
PartialFilter("email")
```

**After:**
```kotlin
// Type-safe with compile-time checking
ExactFilter(User::username)
PartialFilter(User::email)
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## ☕ Support

If you find this library helpful, consider buying me a coffee!

[![Buy Me A Coffee](./buy-me-coffee.png)](https://www.buymeacoffee.com/robertomike)

## ⚠️ Warning

This library currently doesn't support automatic Swagger/OpenAPI definitions. You'll need to document your filter parameters manually in your API documentation.

## 📚 Additional Resources

- [GitHub Repository](https://github.com/RobertoMike/Baradum)
- [Issue Tracker](https://github.com/RobertoMike/Baradum/issues)
- [Changelog](CHANGELOG.md)
- [Migration Guide](MIGRATION_GUIDE.md)

---

**Made with ❤️ by Roberto Mike**
