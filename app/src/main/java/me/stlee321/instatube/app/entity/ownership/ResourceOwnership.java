package me.stlee321.instatube.app.entity.ownership;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "resource_ownership")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ResourceOwnership {
    public ResourceOwnership() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "handle", nullable = false)
    private String handle;
    @Column(name = "resource_id", nullable = false, unique = true)
    private String resourceId;
}
