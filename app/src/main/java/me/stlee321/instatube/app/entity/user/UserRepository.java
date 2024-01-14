package me.stlee321.instatube.app.entity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByHandle(String handle);

    void deleteByHandle(String handle);
}
