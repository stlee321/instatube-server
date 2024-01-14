package me.stlee321.instatube.app.entity.ownership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnershipRepository extends JpaRepository<ResourceOwnership, Long> {
    ResourceOwnership findByResourceId(String resourceId);

    List<ResourceOwnership> findByHandle(String handle);
}
