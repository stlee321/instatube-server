package me.stlee321.instatube.app.graph.repository;

import me.stlee321.instatube.app.graph.node.ReplyNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyNodeRepository extends Neo4jRepository<ReplyNode, Long> {
    @Query("MATCH (u:User)-[:LIKES]->(r:Reply{replyId: $replyId}) RETURN count(u)")
    Integer getLikesCount(@Param("replyId") String replyId);
    @Query("OPTIONAL MATCH l=(u:User{handle: $handle})-[:LIKES]->(r:Reply{replyId: $replyId}) WITH l IS NOT NULL AS l_exist RETURN l_exist")
    Boolean isLikedBy(@Param("replyId") String replyId, @Param("handle") String handle);
//    @Query("MATCH (u:User{handle: $handle})-[:LIKES]->(r:Reply{postId: $postId]) RETURN r")
//    List<ReplyNode> getLikedRepliesInPostBy(@Param("postId") String postId, @Param("handle") String handle);
    @Query("MERGE (u:User{handle: $handle}) MERGE (r:Reply{replyId: $replyId}) MERGE (u)-[:LIKES]->(r)")
    void like(@Param("handle") String handle, @Param("replyId") String replyId);
    @Query("MATCH (u:User{handle: $handle})-[l:LIKES]->(r:Reply{replyId: $replyId}) DELETE l")
    void unlike(@Param("handle") String handle, @Param("replyId") String replyId);
}
