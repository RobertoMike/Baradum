package io.github.robertomike.baradum.querydsl

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

/**
 * Base test class that provides JPA/Hibernate setup with H2 database
 */
abstract class BaseJpaTest {
    
    companion object {
        private lateinit var emf: EntityManagerFactory
        private lateinit var em: EntityManager
        
        @JvmStatic
        @BeforeAll
        fun setUpClass() {
            // Create EntityManagerFactory with H2 database configuration
            val properties = mapOf(
                "jakarta.persistence.jdbc.url" to "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "jakarta.persistence.jdbc.driver" to "org.h2.Driver",
                "jakarta.persistence.jdbc.user" to "sa",
                "jakarta.persistence.jdbc.password" to "",
                "hibernate.dialect" to "org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto" to "create-drop",
                "hibernate.show_sql" to "false",
                "hibernate.format_sql" to "true"
            )
            
            emf = Persistence.createEntityManagerFactory("test-pu", properties)
            em = emf.createEntityManager()
        }
        
        @JvmStatic
        @AfterAll
        fun tearDownClass() {
            if (::em.isInitialized && em.isOpen) {
                em.close()
            }
            if (::emf.isInitialized && emf.isOpen) {
                emf.close()
            }
        }
    }
    
    protected val entityManager: EntityManager
        get() = em
    
    @BeforeEach
    fun setUp() {
        // Start a new transaction for each test
        if (!entityManager.transaction.isActive) {
            entityManager.transaction.begin()
        }
    }
    
    protected fun commit() {
        if (entityManager.transaction.isActive) {
            entityManager.transaction.commit()
        }
    }
    
    protected fun rollback() {
        if (entityManager.transaction.isActive) {
            entityManager.transaction.rollback()
        }
    }
}
