package me.stlee321.instatube.app.entity.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p from Post p where p.timestamp <= :timestamp order by p.timestamp desc")
    List<Post> getPostsBefore(@Param("timestamp") Long timestamp, Pageable pageable);
    @Query("select p from Post p where p.timestamp <= :timestamp and p.handle = :handle order by p.timestamp desc")
    List<Post> getUserPostsBefore(
            @Param("timestamp") Long timestamp,
            @Param("handle") String handle,
            Pageable pageable);

    @Query("select p from Post p where p.timestamp >= :timestamp and p.handle = :handle order by p.timestamp desc")
    List<Post> getUserPostsAfter(
            @Param("timestamp") Long timestamp,
            @Param("handle") String handle,
            Pageable pageable);

    Optional<Post> findByPostId(String postId);

    Long countByHandle(String handle);
    void deleteByHandle(String handle);

    List<Post> findByHandle(String handle);
}
