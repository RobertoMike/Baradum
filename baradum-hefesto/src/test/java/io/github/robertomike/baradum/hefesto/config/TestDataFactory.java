package io.github.robertomike.baradum.hefesto.config;

import io.github.robertomike.baradum.hefesto.models.Status;
import io.github.robertomike.baradum.hefesto.models.User;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper class to create test data for integration tests
 */
public class TestDataFactory {
    
    public static List<User> createTestUsers(EntityManager em) {
        List<User> users = new ArrayList<>();
        
        em.getTransaction().begin();
        
        // User 1: John Doe
        User john = new User();
        john.setUsername("johndoe");
        john.setEmail("john@example.com");
        john.setFullName("John Doe");
        john.setStatus(Status.ACTIVE);
        john.setAge(30);
        john.setCountry("USA");
        john.setSalary(75000.0);
        john.setIsActive(true);
        john.setCreatedAt(createDate(2023, 1, 15));
        em.persist(john);
        users.add(john);
        
        // User 2: Jane Smith
        User jane = new User();
        jane.setUsername("janesmith");
        jane.setEmail("jane@example.com");
        jane.setFullName("Jane Smith");
        jane.setStatus(Status.ACTIVE);
        jane.setAge(28);
        jane.setCountry("USA");
        jane.setSalary(80000.0);
        jane.setIsActive(true);
        jane.setCreatedAt(createDate(2023, 2, 20));
        em.persist(jane);
        users.add(jane);
        
        // User 3: Bob Johnson
        User bob = new User();
        bob.setUsername("bobjohnson");
        bob.setEmail("bob@test.com");
        bob.setFullName("Bob Johnson");
        bob.setStatus(Status.INACTIVE);
        bob.setAge(45);
        bob.setCountry("Canada");
        bob.setSalary(90000.0);
        bob.setIsActive(false);
        bob.setCreatedAt(createDate(2023, 3, 10));
        em.persist(bob);
        users.add(bob);
        
        // User 4: Alice Williams
        User alice = new User();
        alice.setUsername("alicewilliams");
        alice.setEmail("alice@example.com");
        alice.setFullName("Alice Williams");
        alice.setStatus(Status.PENDING);
        alice.setAge(35);
        alice.setCountry("UK");
        alice.setSalary(70000.0);
        alice.setIsActive(true);
        alice.setCreatedAt(createDate(2023, 4, 5));
        em.persist(alice);
        users.add(alice);
        
        // User 5: Charlie Brown
        User charlie = new User();
        charlie.setUsername("charliebrown");
        charlie.setEmail("charlie@test.com");
        charlie.setFullName("Charlie Brown");
        charlie.setStatus(Status.BANNED);
        charlie.setAge(22);
        charlie.setCountry("USA");
        charlie.setSalary(50000.0);
        charlie.setIsActive(false);
        charlie.setCreatedAt(createDate(2023, 5, 12));
        em.persist(charlie);
        users.add(charlie);
        
        // User 6: Diana Prince
        User diana = new User();
        diana.setUsername("dianaprince");
        diana.setEmail("diana@example.com");
        diana.setFullName("Diana Prince");
        diana.setStatus(Status.ACTIVE);
        diana.setAge(32);
        diana.setCountry("USA");
        diana.setSalary(85000.0);
        diana.setIsActive(true);
        diana.setCreatedAt(createDate(2023, 6, 18));
        em.persist(diana);
        users.add(diana);
        
        // User 7: Eve Davis
        User eve = new User();
        eve.setUsername("evedavis");
        eve.setEmail("eve@test.com");
        eve.setFullName("Eve Davis");
        eve.setStatus(Status.ACTIVE);
        eve.setAge(27);
        eve.setCountry("Canada");
        eve.setSalary(65000.0);
        eve.setIsActive(true);
        eve.setCreatedAt(createDate(2023, 7, 22));
        em.persist(eve);
        users.add(eve);
        
        // User 8: Frank Miller
        User frank = new User();
        frank.setUsername("frankmiller");
        frank.setEmail("frank@example.com");
        frank.setFullName("Frank Miller");
        frank.setStatus(Status.INACTIVE);
        frank.setAge(50);
        frank.setCountry("UK");
        frank.setSalary(95000.0);
        frank.setIsActive(false);
        frank.setCreatedAt(createDate(2023, 8, 30));
        em.persist(frank);
        users.add(frank);
        
        em.getTransaction().commit();
        
        return users;
    }
    
    private static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public static void clearDatabase(EntityManager em) {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
    }
}
