package io.github.robertomike.baradum.hefesto.models;

import io.github.robertomike.hefesto.models.BaseModel;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "users")
public class User implements BaseModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(name = "username", nullable = false, unique = true)
    public String username;
    
    @Column(name = "email", nullable = false)
    public String email;
    
    @Column(name = "full_name")
    public String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public Status status;
    
    @Column(name = "age")
    public Integer age;
    
    @Column(name = "country")
    public String country;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    public Date createdAt;
    
    @Column(name = "salary")
    public Double salary;
    
    @Column(name = "is_active")
    public Boolean isActive;

    // BaseModel implementation
    @Override
    public String getTable() {
        return "users";
    }

    // Constructors
    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
