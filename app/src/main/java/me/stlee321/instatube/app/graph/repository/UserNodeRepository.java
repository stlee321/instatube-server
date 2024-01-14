package me.stlee321.instatube.app.graph.repository;

import me.stlee321.instatube.app.graph.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {
    @Query("MATCH (u:User {handle: $handle})<-[:FOLLOWS]-(followers: User) RETURN followers.handle")
    List<String> getAllFollowers(@Param("handle") String handle);

    @Query("MATCH (u:User {handle: $handle})-[:FOLLOWS]->(followings:User) RETURN followings.handle")
    List<String> getAllFollowings(@Param("handle") String handle);
    @Query("MATCH (u:User {handle: $handle})<-[:FOLLOWS]-(followers: User) RETURN count(followers)")
    Integer getFollowerCount(@Param("handle") String handle);
    @Query("MATCH (u:User {handle: $handle})-[:FOLLOWS]->(following:User) RETURN count(following)")
    Integer getFollowingCount(@Param("handle") String handle);
    @Query("OPTIONAL MATCH p=(u:User {handle: $from})-[:FOLLOWS]->(v:User {handle: $to}) WITH p IS NOT NULL AS p_exist RETURN p_exist")
    Boolean isFollowing(@Param("from") String from, @Param("to") String to);
    @Query("UNWIND $tos AS target OPTIONAL MATCH p=(u:User {handle: $from})-[:FOLLOWS]->(v:User {handle: target}) WITH p IS NOT NULL AS p_exist RETURN p_exist")
    List<Boolean> isFollowingMany(@Param("from") String from, @Param("tos") List<String> tos);

    @Query("MERGE (u:User{handle: $from}) MERGE (v:User{handle: $to}) MERGE (u)-[:FOLLOWS]->(v)")
    void follow(@Param("from") String from, @Param("to") String to);

    @Query("MATCH (u:User{handle: $from})-[r:FOLLOWS]->(v:User{handle: $to}) DELETE r")
    void unfollow(@Param("from") String from, @Param("to") String to);

    @Query("MATCH (u:User{handle: $handle}) DETACH DELETE u")
    void deleteUser(@Param("handle") String handle);
}
