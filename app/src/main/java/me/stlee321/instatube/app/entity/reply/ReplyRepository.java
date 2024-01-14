package me.stlee321.instatube.app.entity.reply;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("select r from Reply r where r.timestamp >= :timestamp and r.postId = :postId and r.targetId = '' order by r.timestamp")
    List<Reply> getRepliesAfter(
            @Param("timestamp") Long timestamp,
            @Param("postId") String postId,
            Pageable pageable);

    @Query("select r from Reply r where r.timestamp >= :timestamp and r.postId = :postId and r.targetId = :targetId order by r.timestamp")
    List<Reply> getRepliesAfterForTarget(
            @Param("timestamp") Long timestamp,
            @Param("postId") String postId,
            @Param("targetId") String targetId,
            Pageable pageable);

    Reply findByReplyId(String replyId);

    @Query("select count(r.id) from Reply r where r.targetId = :replyId")
    String getReplyCountOfReply(@Param("replyId") String replyId);

    @Query("select count(r.id) from Reply r where r.postId = :postId")
    String getReplyCountOfPost(@Param("postId") String postId);

    @Query("delete from Reply r where r.targetId = :targetId")
    @Modifying
    void deleteRepliesTargeting(@Param("targetId") String targetId);

    List<Reply> findByHandle(String handle);
    void deleteByHandle(String handle);
}
