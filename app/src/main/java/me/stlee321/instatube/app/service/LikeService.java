package me.stlee321.instatube.app.service;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.graph.node.ReplyNode;
import me.stlee321.instatube.app.graph.node.UserNode;
import me.stlee321.instatube.app.graph.repository.PostNodeRepository;
import me.stlee321.instatube.app.graph.repository.ReplyNodeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LikeService {
    private final PostNodeRepository postNodeRepository;
    private final ReplyNodeRepository replyNodeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_POST_LIKE_PREFIX = "like:post:";
    private static final String REDIS_REPLY_LIKE_PREFIX = "like:reply:";

    public LikeService(
            PostNodeRepository postNodeRepository,
            ReplyNodeRepository replyNodeRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.postNodeRepository = postNodeRepository;
        this.replyNodeRepository = replyNodeRepository;
        this.redisTemplate = redisTemplate;
    }
    public int getPostLikes(String postId) {
        String likes = redisTemplate.opsForValue().get(REDIS_POST_LIKE_PREFIX + postId);
        if(likes == null) {
            int count = postNodeRepository.getLikesCount(postId);
            redisTemplate.opsForValue()
                    .set(REDIS_POST_LIKE_PREFIX + postId, Integer.toString(count), 1L, TimeUnit.DAYS);
            return count;
        }
        return Integer.parseInt(likes);
    }

    public int getReplyLikes(String replyId) {
        String likes = redisTemplate.opsForValue().get(REDIS_REPLY_LIKE_PREFIX + replyId);
        if(likes == null) {
            int count = replyNodeRepository.getLikesCount(replyId);
            redisTemplate.opsForValue()
                    .set(REDIS_REPLY_LIKE_PREFIX + replyId, Integer.toString(count), 1L, TimeUnit.DAYS);
            return count;
        }
        return Integer.parseInt(likes);
    }

    public boolean isPostLikedBy(String postId, String handle) {
        return postNodeRepository.isLikedBy(postId, handle);
    }

    public boolean isReplyLikedBy(String replyId, String handle) {
        return replyNodeRepository.isLikedBy(replyId, handle);
    }

    public boolean likePost(String postId, String me) {
        getPostLikes(postId);
        redisTemplate.opsForValue().increment(REDIS_POST_LIKE_PREFIX + postId);
        try {
            postNodeRepository.like(me, postId);
        }catch(Exception e ) {
            return false;
        }
        return true;
    }

    public boolean unlikePost(String postId, String me) {
        getPostLikes(postId);
        Long count = redisTemplate.opsForValue().decrement(REDIS_POST_LIKE_PREFIX + postId);
        if(count != null && count < 0) {
            redisTemplate.opsForValue().increment(REDIS_POST_LIKE_PREFIX + postId);
        }
        try {
            postNodeRepository.unlike(me, postId);
        }catch(Exception e) {
            return false;
        }
        return true;
    }

    public boolean likeReply(String replyId, String me) {
        getReplyLikes(replyId);
        redisTemplate.opsForValue().increment(REDIS_REPLY_LIKE_PREFIX + replyId);
        try {
            replyNodeRepository.like(me, replyId);
        }catch(Exception e ) {
            return false;
        }
        return true;
    }

    public boolean unlikeReply(String replyId, String me) {
        getReplyLikes(replyId);
        Long count = redisTemplate.opsForValue().decrement(REDIS_REPLY_LIKE_PREFIX + replyId);
        if(count != null && count < 0) {
            redisTemplate.opsForValue().increment(REDIS_REPLY_LIKE_PREFIX + replyId);
        }
        try {
            replyNodeRepository.unlike(me, replyId);
        }catch(Exception e) {
            return false;
        }
        return true;
    }
}
