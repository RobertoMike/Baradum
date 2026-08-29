package io.github.robertomike.baradum.hefesto

import io.github.robertomike.hefesto.models.BaseModel
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for HefestoBaradum, and for the deprecated `Baradum` object kept as a compatibility
 * shim after the rename (for consistency with QueryDslBaradum).
 */
class HefestoBaradumTest {

    @Entity
    @Table(name = "hefesto_baradum_test_models")
    class TestModel : BaseModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null

        override fun getTable(): String = "hefesto_baradum_test_models"
    }

    @Test
    fun `HefestoBaradum make returns a Baradum instance backed by HefestoQueryBuilder`() {
        val baradum = HefestoBaradum.make(TestModel::class.java)

        assertNotNull(baradum)
        assertTrue(baradum.getBuilder() is HefestoQueryBuilder<*>)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `deprecated Baradum object still works and delegates to HefestoBaradum`() {
        val baradum = Baradum.make(TestModel::class.java)

        assertNotNull(baradum)
        assertTrue(baradum.getBuilder() is HefestoQueryBuilder<*>)
    }
}
