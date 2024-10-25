package io.github.robertomike.baradum.models;

import io.github.robertomike.baradum.converters.StatusConverter;
import io.github.robertomike.hefesto.models.HibernateModel;
import javax.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements HibernateModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String photo;
    @Column(name = "status")
    @Convert(converter = StatusConverter.class)
    private Set<Status> status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_pet",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = @JoinColumn(name = "pet_id"))
    @ToString.Exclude
    private Set<Pet> pets;

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
