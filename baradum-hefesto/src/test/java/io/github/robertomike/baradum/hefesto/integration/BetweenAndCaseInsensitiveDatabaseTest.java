package io.github.robertomike.baradum.hefesto.integration;

import io.github.robertomike.baradum.core.Baradum;
import io.github.robertomike.baradum.core.enums.BaradumOperator;
import io.github.robertomike.baradum.core.enums.WhereOperator;
import io.github.robertomike.baradum.core.filters.PartialFilter;
import io.github.robertomike.baradum.hefesto.HefestoQueryBuilder;
import io.github.robertomike.baradum.hefesto.config.DatabaseExtension;
import io.github.robertomike.baradum.hefesto.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the BETWEEN and LIKE_IGNORE_CASE operators against a real database.
 * Both are implemented in HefestoQueryBuilder via raw Criteria API predicates (whereCustom),
 * since Hefesto's own Operator enum has neither natively - a pure unit test can't verify the
 * generated SQL actually executes correctly, so this exercises them end to end.
 */
@ExtendWith(DatabaseExtension.class)
class BetweenAndCaseInsensitiveDatabaseTest {

    @Test
    void testBetweenOperatorOnAge() {
        HefestoQueryBuilder<User> qb = new HefestoQueryBuilder<>(User.class);
        List<User> users = qb.where("age", BaradumOperator.BETWEEN, List.of(27, 32), WhereOperator.AND).get();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users aged 27-32");
        users.forEach(user -> assertTrue(user.getAge() >= 27 && user.getAge() <= 32,
                "User age should be between 27 and 32: " + user.getAge()));
    }

    @Test
    void testBetweenOperatorExcludesOutOfRangeRows() {
        HefestoQueryBuilder<User> qb = new HefestoQueryBuilder<>(User.class);
        List<User> users = qb.where("age", BaradumOperator.BETWEEN, List.of(100, 120), WhereOperator.AND).get();

        assertNotNull(users);
        assertTrue(users.isEmpty(), "No user is aged 100-120");
    }

    @Test
    void testBetweenOperatorOnSalary() {
        HefestoQueryBuilder<User> qb = new HefestoQueryBuilder<>(User.class);
        List<User> users = qb.where("salary", BaradumOperator.BETWEEN, List.of(70000.0, 80000.0), WhereOperator.AND).get();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find users with salary 70k-80k");
        users.forEach(user -> assertTrue(user.getSalary() >= 70000.0 && user.getSalary() <= 80000.0,
                "User salary should be between 70000 and 80000: " + user.getSalary()));
    }

    @Test
    void testLikeIgnoreCaseFindsRowsRegardlessOfCase() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "JOHN");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("search", "fullName").setIgnoreCase(true))
                .withParams(params)
                .get();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should find 'John Doe' when searching for 'JOHN'");
        assertTrue(users.stream().anyMatch(u -> u.getFullName().equals("John Doe")));
    }

    @Test
    void testLikeIgnoreCaseWithNoMatchReturnsEmpty() {
        Map<String, String> params = new HashMap<>();
        params.put("search", "NOSUCHNAME");

        List<User> users = Baradum.make(User.class)
                .allowedFilters(new PartialFilter("search", "fullName").setIgnoreCase(true))
                .withParams(params)
                .get();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}
