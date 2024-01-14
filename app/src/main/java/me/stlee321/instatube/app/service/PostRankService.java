package me.stlee321.instatube.app.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostRankService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String POPULAR_POST_KEY = "popular:post";
    private static final double CREATED = 1.0;
    private static final double VIEWED = 1.0;
    private static final double LIKED = 3.0;
    private static final double COMMENTED = 2.0;
    public PostRankService(
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    private void increaseScore(String postId, double increment) {
        redisTemplate.opsForZSet().incrementScore(POPULAR_POST_KEY, postId, increment);
    }

    public void postCreated(String postId) {
        increaseScore(postId, CREATED);
    }
    public void postViewed(String postId) {
        increaseScore(postId, VIEWED);
    }

    public void postLiked(String postId) {
        increaseScore(postId, LIKED);
    }
    public void postCommented(String postId) {
        increaseScore(postId, COMMENTED);
    }
    public void removePost(String postId) {
        redisTemplate.opsForZSet().remove(POPULAR_POST_KEY, postId);
    }

    public List<String> getPopularPosts(long n) {
        var popularSet = redisTemplate.opsForZSet()
                .reverseRange(POPULAR_POST_KEY, 0, n-1);
        if(popularSet == null) {
            return new ArrayList<>();
        }
        return popularSet.stream().toList();
    }
}
