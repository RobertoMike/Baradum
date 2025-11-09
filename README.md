# Welcome to Baradum

[![Maven Central](https://img.shields.io/maven-central/v/io.github.robertomike/baradum.svg)](https://search.maven.org/artifact/io.github.robertomike/baradum)
[![License](https://img.shields.io/github/license/RobertoMike/Baradum)](LICENSE.txt)

Baradum is a powerful library that simplifies filtering and sorting in your Java/Kotlin applications using HefestoSql. It allows you to dynamically filter and sort database queries using URL parameters or request body, eliminating the need for complex conditional logic.

## 🚀 What's New in Latest Version

- **🎯 Type-Safe Kotlin API**: Use property references (`User::name`) for compile-time safety
- **📅 Enhanced DateFilter**: Support for LocalDate, LocalDateTime, java.util.Date, SQL dates with custom patterns
- **🔢 New Comparison Filters**: GreaterFilter and LessFilter with configurable equality
- **🏗️ Builder Patterns**: Fluent APIs for complex filter configurations
- **📚 Comprehensive Documentation**: Complete API reference and migration guides

## 📋 Table of Contents

- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Filter Types](#-filter-types)
- [Kotlin Property References](#-kotlin-property-references-type-safe-api)
- [Date Filtering](#-date-filtering)
- [Sorting](#-sorting)
- [Body Filtering](#-body-based-filtering)
- [Configuration](#-configuration)
- [Documentation](#-documentation)
- [Examples](#-examples)

## 📦 Installation

### Core Library

<table>
<tr><th>Maven</th><th>Gradle</th></tr>
<tr>
<td>

```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum</artifactId>
    <version>2.1.1</version>
</dependency>
```

</td>
<td>

```gradle
dependencies {
    implementation 'io.github.robertomike:baradum:2.1.1'
}
```

</td>
</tr>
</table>

### Spring Boot Integration

<table>
<tr><th>Maven</th><th>Gradle</th></tr>
<tr>
<td>

```xml
<!-- Spring Boot 2 -->
<dependency>
  <groupId>io.github.robertomike</groupId>
  <artifactId>baradum-apache-tomcat</artifactId>
  <version>1.0.1</version>
</dependency>

<!-- Spring Boot 3 -->
<dependency>
  <groupId>io.github.robertomike</groupId>
  <artifactId>baradum-apache-tomcat</artifactId>
  <version>2.0.3</version>
</dependency>
```

</td>
<td>

```gradle
dependencies {
    // Spring Boot 2
    implementation 'io.github.robertomike:baradum-apache-tomcat:1.0.1'
    
    // Spring Boot 3
    implementation 'io.github.robertomike:baradum-apache-tomcat:2.0.3'
}
```

</td>
</tr>
</table>

## 🎯 Quick Start

### The Problem

**Without Baradum (Traditional Approach):**

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
    if (categoryId != null && minAge != null) {
        return repository.findByCategoryIdAndAgeGreaterThanEqual(categoryId, minAge);
    }
    if (minAge != null && maxAge != null) {
        return repository.findByAgeGreaterThanEqualAndAgeLessThanEqual(minAge, maxAge);
    }
    // ... 5 more conditional branches
    return repository.findAll();
}
```

### The Solution

**With Baradum:**

```java
@GetMapping("/users")
public List<User> getUsers() {
    return Baradum.make(User.class)
        .allowedFilters("categoryId")
        .allowedFilters(new IntervalFilter("age"))
        .page();
}
```

**Kotlin with Type Safety:**

```kotlin
@GetMapping("/users")
fun getUsers(): List<User> {
    return Baradum.make(User::class.java)
        .allowedFilters(
            ExactFilter(User::categoryId),
            IntervalFilter(User::age)
        )
        .page()
}
```

**URL Usage:**
- `?categoryId=2` → Filters by `categoryId = 2`
- `?age=18-65` → Filters by `age >= 18 AND age <= 65`
- `?categoryId=2&age=18-65` → Both filters applied

## 📚 Filter Types

### Core Filters

| Filter | Purpose | Example URL | SQL Result |
|--------|---------|-------------|------------|
| **ExactFilter** | Exact matching | `?status=ACTIVE` | `status = 'ACTIVE'` |
| **PartialFilter** | LIKE search | `?name=john` | `name LIKE 'john%'` |
| **SearchFilter** | Multi-field OR | `?search=john` | `name LIKE '%john%' OR email LIKE '%john%'` |
| **EnumFilter** | Enum values | `?status=ACTIVE,PENDING` | `status IN ('ACTIVE', 'PENDING')` |
| **IntervalFilter** | Numeric ranges | `?age=18-65` | `age >= 18 AND age <= 65` |
| **InFilter** | Multiple values | `?country=US,CA,MX` | `country IN ('US', 'CA', 'MX')` |
| **IsNullFilter** | NULL checks | `?deletedAt=null` | `deletedAt IS NULL` |

### Comparison Filters

| Filter | Purpose | Example URL | SQL Result |
|--------|---------|-------------|------------|
| **ComparisonFilter** | Flexible operators | `?price=>100` | `price > 100` |
| **GreaterFilter** | Greater than | `?age=18` | `age > 18` or `age >= 18` |
| **LessFilter** | Less than | `?age=65` | `age < 65` or `age <= 65` |

### Date & Time Filters

| Filter | Purpose | Example URL | SQL Result |
|--------|---------|-------------|------------|
| **DateFilter** | Date filtering | `?date=2024-01-01\|2024-12-31` | `date BETWEEN '2024-01-01' AND '2024-12-31'` |
| | Comparison | `?date=>2024-01-01` | `date > '2024-01-01'` |

### Advanced

| Filter | Purpose | Example | Description |
|--------|---------|---------|-------------|
| **CustomFilter** | Lambda logic | See docs | Custom filtering logic |

## 🎭 Kotlin Property References (Type-Safe API)

Use Kotlin property references for compile-time safety:

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
            ExactFilter(User::id),                              // ?id=123
            PartialFilter(User::name, "search"),                // ?search=john
            EnumFilter(User::status, Status::class.java),       // ?status=ACTIVE
            IntervalFilter(User::age),                          // ?age=18-65
            DateFilter.forLocalDateTime(User::createdAt),       // ?createdAt=>2024-01-01
            GreaterFilter(User::age, "minAge", orEqual = true), // ?minAge=18
            LessFilter(User::age, "maxAge", orEqual = true)     // ?maxAge=65
        )
        .allowedSort(User::name, User::createdAt)
        .page()
}
```

**Benefits:**
- ✅ Compile-time field validation
- ✅ Refactoring support
- ✅ IDE auto-completion
- ✅ Type safety

## 📅 Date Filtering

### Multiple Date Types Supported

```java
// LocalDate (default)
DateFilter("createdAt")

// LocalDateTime with custom pattern
DateFilter.forLocalDateTime("updatedAt", "dd/MM/yyyy HH:mm:ss")

// java.util.Date
DateFilter.forUtilDate("birthDate", "MM-dd-yyyy")

// SQL Date
DateFilter.forSqlDate("startDate")

// SQL Timestamp
DateFilter.forSqlTimestamp("eventTime")

// Builder pattern
DateFilter.builder("eventDate")
    .useLocalDate()
    .withPattern("yyyy/MM/dd")
    .build()
```

### Date Filtering Examples

| URL Parameter | SQL Result |
|---------------|------------|
| `?date=2024-01-01` | `date = '2024-01-01'` |
| `?date=2024-01-01\|2024-12-31` | `date >= '2024-01-01' AND date <= '2024-12-31'` |
| `?date=>2024-01-01` | `date > '2024-01-01'` |
| `?date=>=2024-01-01` | `date >= '2024-01-01'` |
| `?date=<2024-12-31` | `date < '2024-12-31'` |
| `?date=<=2024-12-31` | `date <= '2024-12-31'` |

## 📊 Sorting

### Simple Sorting

```java
return Baradum.make(User.class)
    .allowedSort("name", "createdAt")
    .get();
```

**URL Examples:**
- `?sort=name` → `ORDER BY name ASC`
- `?sort=-name` → `ORDER BY name DESC`
- `?sort=name,-createdAt` → `ORDER BY name ASC, createdAt DESC`

### Custom Field Names

```java
.allowedSort(new OrderBy("alias", "actual_field_name"))
```

### Kotlin Property References

```kotlin
.allowedSort(User::name, User::createdAt, User::email)
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

## ⚙️ Configuration

### Spring Boot (Auto-Configuration)

**No configuration needed!** Spring Boot 2 & 3 are auto-configured.

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

## 📖 Documentation

- **[Complete Documentation](DOCUMENTATION.md)** - Full guide with examples
- **[Filter API Reference](FILTER_API_REFERENCE.md)** - Detailed filter documentation
- **[Migration Guide](MIGRATION_GUIDE.md)** - Upgrade from previous versions
- **[Test Suite Summary](TEST_SUITE_SUMMARY.md)** - Test coverage details

## 💡 Examples

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
            InFilter(Product::brand, "brands")
        )
        .allowedSort(Product::price, Product::name)
        .page()
}
```

**Usage:**
```
GET /products?category=electronics&search=laptop&minPrice=500&maxPrice=2000
    &brands=Apple,Dell,HP&status=IN_STOCK&sort=price
```

### User Management

```java
@GetMapping("/users")
public List<User> getUsers() {
    return Baradum.make(User.class)
        .allowedFilters(
            PartialFilter("username"),
            SearchFilter.of("search", "username", "email", "fullName"),
            EnumFilter("role", Role.class),
            DateFilter.forLocalDateTime("createdAt"),
            IntervalFilter("age"),
            IsNullFilter("deletedAt")
        )
        .allowedSort("username", "createdAt")
        .get();
}
```

**Usage:**
```
GET /users?search=john&role=ADMIN&age=25-50&deletedAt=null&sort=-createdAt
```

### Event Calendar

```java
@GetMapping("/events")
public List<Event> getEvents() {
    return Baradum.make(Event.class)
        .allowedFilters(
            ExactFilter("organizerId"),
            PartialFilter("title"),
            DateFilter.builder("eventDate")
                .useLocalDateTime()
                .withPattern("yyyy-MM-dd HH:mm")
                .build(),
            GreaterFilter("attendeeCount", "minAttendees", true),
            InFilter("location")
        )
        .allowedSort("eventDate", "title")
        .page();
}
```

**Usage:**
```
GET /events?title=concert&eventDate=>2024-01-01&minAttendees=100
    &location=NY,LA,CHI&sort=eventDate
```

## 🔧 Advanced Features

### Default Values

```java
ExactFilter("status").setDefaultValue("ACTIVE")
```

### Ignoring Values

```java
IntervalFilter("age").addIgnore("0", "null", "")
```

### Custom Filters

```java
new CustomFilter<>("status", (query, value) -> {
    if (value.equals("premium")) {
        query.where("subscription_level", BaradumOperator.GREATER, 5);
    } else {
        query.where("status", BaradumOperator.EQUAL, value);
    }
})
```

## ⚠️ Important Notes

- **Swagger/OpenAPI**: Manual documentation required (no auto-generation support)
- **Spring Boot**: Versions 2 & 3 supported
- **Apache Tomcat**: Versions 9 & 10 supported

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## ☕ Support

If you find this library helpful, consider buying me a coffee!

[![Buy Me A Coffee](./buy-me-coffee.png)](https://www.buymeacoffee.com/robertomike)

---

**Made with ❤️ by Roberto Mike**
