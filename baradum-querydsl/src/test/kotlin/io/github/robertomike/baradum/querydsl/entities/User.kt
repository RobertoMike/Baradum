package io.github.robertomike.baradum.querydsl.entities

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "email", nullable = false, unique = true)
    val email: String,
    
    @Column(name = "age")
    val age: Int,
    
    @Column(name = "is_active")
    val isActive: Boolean = true,
    
    @Column(name = "country")
    val country: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: UserStatus = UserStatus.ACTIVE,
    
    @Column(name = "birth_date")
    val birthDate: LocalDate? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "salary")
    val salary: Double? = null
)

enum class UserStatus {
    ACTIVE, INACTIVE, PENDING, SUSPENDED
}
