package me.stlee321.instatube.app.graph.repository;

import me.stlee321.instatube.app.graph.node.PostNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostNodeRepository extends Neo4jRepository<PostNode, Long> {
    @Query("MATCH (u:User)-[:LIKES]->(p:Post{postId: $postId}) RETURN count(u)")
    Integer getLikesCount(@Param("postId") String postId);
    @Query("OPTIONAL MATCH l=(u:User{handle: $handle})-[:LIKES]->(p:Post{postId: $postId}) WITH l IS NOT NULL AS l_exist RETURN l_exist")
    Boolean isLikedBy(@Param("postId") String postId, @Param("handle") String handle);
    @Query("MERGE (u:User{handle: $handle}) MERGE (p: Post{postId: $postId}) MERGE (u)-[:LIKES]->(p)")
    void like(@Param("handle") String handle, @Param("postId") String postId);
    @Query("MATCH (u:User{handle: $handle})-[r:LIKES]->(p:Post{postId: $postId}) DELETE r")
    void unlike(@Param("handle") String handle, @Param("postId") String postId);
}
