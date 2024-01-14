package me.stlee321.instatube.app.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.entity.BaseEntity;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User extends BaseEntity {
    public User() {}
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "handle", unique = true, nullable = false)
    private String handle;
    @Column(name = "avatar_id")
    private String avatarId;
}
