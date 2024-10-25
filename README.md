# Welcome to Baradum

This library is for simplify work when we need to filter or sort the models in a simple way using HefestoSql.
This will allow you to filter or sort your models using Parameters or body in a simple way

## Links
- [Example of use](#example-of-use)
- [How to install](#how-to-install)
- [Difference](#difference)
- [Configuration of Baradum](#configuration)
- [Allowed filters for parameters](#allowed-filters-for-parameters)
- [How to sort using parameters](#how-to-sort-using-parameters)
- [How to filter and sort by body](#how-to-filter-and-sort-using-body)
- [Warning](#warning-)

##  Example of use

If repository return null or optional empty throw NotFoundException.

```java
package io.github.robertomike.baradum.Baradum;

public class controller {
    public List<User> nameMethod() {
        return Baradum.make(User.class)
                // this will allow to filter using a param named id
                .allowedFilters("id")
                .get();
    }
}
```

## How to install

If you only need the baradum library you can use this, 
but you will need to create a custom request class, see: [How to create custom request class](#create-custom-request-class)

Maven
```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <artifactId>baradum</artifactId>
    <version>1.0.1</version>
</dependency>
```
Gradle
```gradle
dependencies {
    implementation 'io.github.robertomike:baradum:1.0.1'
}
```

If you want use it on Spring boot 2, 3 or with apache tomcat you need to use

Maven
```xml
<dependency>
    <groupId>io.github.robertomike</groupId>
    <!--  For spring boot 2  -->
    <artifactId>baradum-apache-tomcat-9</artifactId>
    <!--  For spring boot 3  -->
    <artifactId>baradum-apache-tomcat-10</artifactId>
    <version>2.0.1</version>
</dependency>
```
Gradle
```gradle
dependencies {
    // Spring boot 2
    implementation 'io.github.robertomike:baradum-apache-tomcat-9:2.0.1'
    // Spring boot 3
    implementation 'io.github.robertomike:baradum-apache-tomcat-10:2.0.1'
}
```

##  Difference

<table>
<tr>
    <th>Normal</th>
    <th>Baradum</th>
</tr>
<tr>
<td>

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
public class ExampleController {
    private ExampleRepository repository;

    @GetMapping("/examples")
    public Example index(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long minAge,
            @RequestParam(required = false) Long maxAge
        ) {
        if (categoryId != null && minAge != null && maxAge != null) {
            return repository.findByCategoryIdAndAgeGreaterThanEqualAndAgeLessThanEqual(status, minAge, maxAge);
        }
        if (categoryId != null && minAge != null) {
            return repository.findByCategoryIdAndAgeGreaterThanEqual(status, minAge);
        }
        if (minAge != null && maxAge != null) {
            return repository.findByAgeGreaterThanEqualAndAgeLessThanEqual(minAge, maxAge);
        }
        if (categoryId != null) {
            return repository.findBycategoryId(status);
        }
        if (minAge != null) {
            return repository.findByAgeGreaterThanEqual(minAge);
        }
        if (maxAge != null) {
            return repository.findByAgeLessThanEqual(maxAge);
        }
        
        return repository.findAll();
    }
}
```
</td>
<td>

```java
import io.github.robertomike.baradum.Baradum;
import org.springframework.web.bind.annotation.GetMapping;

public class ExampleController {
    @GetMapping("/examples")
    public Example index() {
        return Baradum.make(User.class)
                // This will create a ExactFilter, and with that if this find the param will be filtered by and equals
                // example "?categoryId=2" this will be filtered by categoryId = 2
                .allowedFilters("categoryId")
                .allowedFilters(
                        // The conditions will be applied the conditions based on the param age
                        // example "?age=18" this will be filtered by age >= 18
                        // example "?age=18,65" this will be filtered by age >= 18 and age <= 65
                        // example "?age=,65" this will be filtered by age <= 65
                        new IntervalFilter("age")
                )
                .page();
    }
}
```
</td>
</tr>
<tr>
<td>

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
public class ExampleController {
    private ExampleRepository repository;

    @GetMapping("/examples")
    public Example index(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Enum status
        ) {
        if (status != null && name != null) {
            return repository.findByStatusAndName(status, name);
        }
        if (status != null) {
            return repository.findByStatus(status);
        }
        if (name != null) {
            return repository.findByName(name);
        }
        
        return repository.findAll();
    }
}
```
</td>
<td>

```java
import io.github.robertomike.baradum.Baradum;
import org.springframework.web.bind.annotation.GetMapping;

public class ExampleController {
    @GetMapping("/examples")
    public Example index() {
        return Baradum.make(User.class)
                .allowedFilters(
                        // This allows you to filter using like and add automatically the % at the end of the value
                        // Example "?name=ale" will be filtered as "name like 'ale%'"
                        new PartialFilter("name"),
                        // This receives the value and transform it to the enum you need
                        new EnumFilter<>("status", Enum.class)
                )
                .get();
    }
}
```
</td>
</tr>
</table>

##  Configuration

If you are using Spring boot 2 or 3 you don't need to make nothing.

The configuration is simple, if you are using a framework that use the library org.apache.tomcat.embed:tomcat-embed-core 
for the request, you only need to use the class based on your version. Supported versions are 9 and 10 of apache tomcat

```java

public class BaradumConfig {
    private BaradumConfig(HttpServletRequest request) {
        // if you are using apache tomcat 9
        new AutoConfigurationSpring2(request);
        // if you are using apache tomcat 10
        new AutoConfigurationSpring3(request);
    }
}
```

### Create custom request class
If you are not using org.apache.tomcat.embed:tomcat-embed-core or the library don't support some version
you can create you own class for the request

This is an example of the ApacheTomcatRequest and what do you need to do
```java

public class ApacheTomcatRequest extends BasicRequest<HttpServletRequest> {
    public ApacheTomcatRequest(HttpServletRequest request) {
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
```

After this you need to set the class that you create and configure Baradum

```java

public class BaradumConfig {
    private BaradumConfig(HttpServletRequest request) {
        Baradum.setRequest(new ApacheTomcatRequest(request));
    }
}
```

##  Allowed filters for Parameters

There is many class for filter, this is the list:

- DateFilter: This allows you to filter by date and select the operator
  - Examples:
    - ?date=01-01-2022 => date = new Date('01-01-2022')
    - ?date=<=01-01-2022 => date <= new Date('01-01-2022')
    - All supported operators are '<,<=,>,>='
- EmptyFilter: This verifies if the field is null or empty
- EnumFilter: Transform the received value into enum and will be compared by equals
  - If you need to pass more than one enum and use In operator you concat the values with a comma ','
- ExactFilter: is the base Filter, only will get the value and make equal to the field
- IntervalDateFilter: This allows you to filter by range of dates
  - Examples:
    - ?date=01-01-2022 => date >= 01-01-2022
    - ?date=01-01-2022,02-01-2022 => 'date >= 01-01-2022 and date <= 02-01-2022'
    - ?date=,01-01-2022 => 'date <= 01-01-2022'
- NotEmptyFilter: This is the opposite of EmptyFilter
- PartialFilter: This allows you to search inside text using Like as operator and putting % at the final value
  - ?name=mar => name like 'mar%'
- SetFilter: This filter will allow you to filter using FIND_IN_SET or NO_FIND_IN_SET function using an Enum
  - If you want to pass more than one parameter you can use , or | and will be a sub query. Example:
    - (find_in_set() and find_in_set() and find_in_set())
  - If you are using | then will be applied the Where operator OR
  - If you are using , then will be applied the Where Operator AND
  - If you want to use the NO_FIND_IN_SET when create the filter need to pass in the constructor false inside not variable
- CustomFilter: this filter allows you to pass a lambda expression where do you receive Hefesto and the value

If you need something more specific you can use the CustomFilter or create your own filter extending the class Filter.

For all the filters are allowed to specify the alias for the field.
Example: 

```java
import io.github.robertomike.baradum.Baradum;
import org.springframework.web.bind.annotation.GetMapping;

public class ExampleController {
    @GetMapping("/examples")
    public Example index() {
        return Baradum.make(User.class)
                .allowedFilters(new ExactFilter("alias", "name"))
                .get();
    }
}
```

Url parameter: ?alias=pap

##  How to sort using Parameters

If you need to allow the sort using parameters you can use Baradum

```java
import io.github.robertomike.baradum.Baradum;
import org.springframework.web.bind.annotation.GetMapping;

public class ExampleController {
    @GetMapping("/examples")
    public Example index() {
        return Baradum.make(User.class)
                .allowedSort("name")
                .allowedSort(new OrderBy("alias", "field"))
                .get();
    }
}
```

In this way you can sort by one or more fields, using comma.
And if you want to define the sort direction you put in front of the field a - for DESC and nothing for ASC

Examples:
- ?sort=name
- ?sort=name,alias
- ?sort=-name,alias

##  How to filter and sort using Body

The difference between the parameters and body is, 
that Filters for the body will only be use for cast and know if is allowed to use that field

### Supported filters are:

- ExactFilter
- EnumFilter
- DateFilter

### Json example of body

This is a json example of how you can filter and sort your results.

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

How you can see the body filter is more advance that the parameters filter.
The body will be read from the class HttpServletRequest and transformed in BodyRequest

## Warning ![Warning](./warning.svg)

This library actually doesn't support swagger for automatically definitions.

[![coffee](./buy-me-coffee.png)](https://www.buymeacoffee.com/robertomike)