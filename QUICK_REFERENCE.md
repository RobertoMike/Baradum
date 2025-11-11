# Baradum Quick Reference

## 🚀 Installation

```gradle
// Hefesto (Hibernate/HefestoSQL)
implementation 'io.github.robertomike:baradum-hefesto:3.0.0'

// OR QueryDSL (Type-safe queries)
implementation 'io.github.robertomike:baradum-querydsl:3.0.0'

// Spring Boot 3 integration
implementation 'io.github.robertomike:baradum-apache-tomcat:3.0.0'
```

## 📋 Filter Cheat Sheet

### Basic Filters

| Filter | Java | Kotlin | URL | SQL |
|--------|------|--------|-----|-----|
| **Exact** | `ExactFilter("id")` | `ExactFilter(User::id)` | `?id=123` | `id = 123` |
| **Partial** | `PartialFilter("name")` | `PartialFilter(User::name)` | `?name=john` | `name LIKE 'john%'` |
| **Search** | `SearchFilter.of("name", "email")` | - | `?search=john` | `name LIKE '%john%' OR email LIKE '%john%'` |
| **Enum** | `EnumFilter("status", Status.class)` | `EnumFilter(User::status, Status::class.java)` | `?status=ACTIVE` | `status = 'ACTIVE'` |
| **Interval** | `IntervalFilter("age")` | `IntervalFilter(User::age)` | `?age=18-65` | `age >= 18 AND age <= 65` |
| **In** | `InFilter("country")` | `InFilter(User::country)` | `?country=US,CA` | `country IN ('US', 'CA')` |
| **IsNull** | `IsNullFilter("deletedAt")` | `IsNullFilter(User::deletedAt)` | `?deletedAt=null` | `deletedAt IS NULL` |

### Comparison Filters

| Filter | Java | Kotlin | URL | SQL |
|--------|------|--------|-----|-----|
| **Comparison** | `ComparisonFilter("price")` | `ComparisonFilter(Product::price)` | `?price=>100` | `price > 100` |
| **Greater** | `GreaterFilter("age")` | `GreaterFilter(User::age)` | `?age=18` | `age > 18` |
| **Greater≥** | `GreaterFilter("age", true)` | `GreaterFilter(User::age, orEqual=true)` | `?age=18` | `age >= 18` |
| **Less** | `LessFilter("age")` | `LessFilter(User::age)` | `?age=65` | `age < 65` |
| **Less≤** | `LessFilter("age", true)` | `LessFilter(User::age, orEqual=true)` | `?age=65` | `age <= 65` |

### Date Filters

| Type | Java | Kotlin |
|------|------|--------|
| **LocalDate** | `DateFilter.forLocalDate("date")` | `DateFilter.forLocalDate(Event::date)` |
| **LocalDateTime** | `DateFilter.forLocalDateTime("time", "dd/MM/yyyy HH:mm")` | `DateFilter.forLocalDateTime(Event::time, "...")` |
| **java.util.Date** | `DateFilter.forUtilDate("birth", "MM-dd-yyyy")` | `DateFilter.forUtilDate(User::birth, "...")` |
| **Builder** | `DateFilter.builder("date").useLocalDate().withPattern("yyyy/MM/dd").build()` | - |

**Date URL Examples:**
- `?date=2024-01-01` → `date = '2024-01-01'`
- `?date=2024-01-01|2024-12-31` → `date BETWEEN ...`
- `?date=>2024-01-01` → `date > '2024-01-01'`
- `?date=>=2024-01-01` → `date >= '2024-01-01'`

## 📝 Common Patterns

### Basic Filtering

```java
@GetMapping("/users")
public List<User> getUsers() {
    return Baradum.make(User.class)
        .allowedFilters("id", "username")
        .get();
}
```

### Multiple Filters

```kotlin
Baradum.make(User::class.java)
    .allowedFilters(
        ExactFilter(User::id),
        PartialFilter(User::name, "search"),
        EnumFilter(User::status, Status::class.java),
        IntervalFilter(User::age),
        DateFilter.forLocalDate(User::createdAt)
    )
    .page()
```

### With Sorting

```java
Baradum.make(Product.class)
    .allowedFilters(
        ExactFilter("category"),
        GreaterFilter("price", "minPrice", true),
        LessFilter("price", "maxPrice", true)
    )
    .allowedSort("price", "name")
    .page()
```

**URL:** `?category=electronics&minPrice=100&maxPrice=1000&sort=-price`

### Complete Example

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

**URL:**
```
/products?search=laptop&category=electronics&minPrice=500&maxPrice=2000
         &brands=Apple,Dell&status=IN_STOCK&releaseDate=>2024-01-01
         &sort=price,-releaseDate
```

## 🎯 URL Patterns

### Sorting
- `?sort=name` → ASC
- `?sort=-name` → DESC
- `?sort=name,-age` → Multiple

### Intervals
- `?age=25` → Exact
- `?age=18-65` → Range
- `?age=18-` → Min only
- `?age=-65` → Max only

### Enums
- `?status=ACTIVE` → Single
- `?status=ACTIVE,PENDING` → Multiple (IN)

### Dates
- `?date=2024-01-01` → Exact
- `?date=2024-01-01|2024-12-31` → Range
- `?date=>2024-01-01` → After
- `?date=<=2024-12-31` → Before

### NULL Checks
- `?deleted=null` → IS NULL
- `?deleted=not_null` → IS NOT NULL
- Accepts: `true/false`, `1/0`, `yes/no`

## 🔧 Configuration

### Default Values
```java
ExactFilter("status").setDefaultValue("ACTIVE")
```

### Ignore Values
```java
IntervalFilter("age").addIgnore("0", "null", "")
```

### Custom Logic
```java
new CustomFilter<>("field", (query, value) -> {
    // Your custom logic
    query.where("field", BaradumOperator.EQUAL, value);
})
```

## 📚 Documentation

- **[DOCUMENTATION.md](DOCUMENTATION.md)** - Complete guide
- **[FILTER_API_REFERENCE.md](FILTER_API_REFERENCE.md)** - API reference
- **[ENHANCEMENT_SUMMARY.md](ENHANCEMENT_SUMMARY.md)** - What's new

## ⚡ Quick Tips

1. **Use Kotlin property references** for type safety
2. **Set default values** for required filters
3. **Ignore invalid values** to prevent errors
4. **Choose specific filters** (GreaterFilter vs ComparisonFilter)
5. **Use builder patterns** for complex configs
6. **Document your API** (Swagger requires manual docs)

## 🆘 Common Issues

### Date Parsing Fails
```java
// Solution: Specify type and pattern
DateFilter.forUtilDate("date", "dd/MM/yyyy")
```

### Enum Not Found
```java
// Solution: Ensure enum class is passed
EnumFilter("status", Status.class)  // Required!
```

### Field Name Mismatch
```kotlin
// Solution: Use property reference (compile-time check)
ExactFilter(User::userName)  // Not "username"
```

---

**Need more help?** Check [DOCUMENTATION.md](DOCUMENTATION.md) or [GitHub Issues](https://github.com/RobertoMike/Baradum/issues)
