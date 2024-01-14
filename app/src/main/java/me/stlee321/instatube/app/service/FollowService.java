package me.stlee321.instatube.app.service;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.graph.node.UserNode;
import me.stlee321.instatube.app.graph.repository.UserNodeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FollowService {

    private final UserNodeRepository userNodeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_FOLLOWER_COUNT_PREFIX = "follow:follower:count:";
    private static final String REDIS_FOLLOWING_COUNT_PREFIX = "follow:following:count:";
    private static final String REDIS_FOLLOWER_HANDLES_PREFIX = "follow:follower:handles:";
    private static final String REDIS_FOLLOWING_HANDLES_PREFIX = "follow:following:handles:";
    public FollowService(
            UserNodeRepository userNodeRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.userNodeRepository = userNodeRepository;
        this.redisTemplate = redisTemplate;
    }
    public boolean follow(String me, String target) {
        getFollowerCount(target);
        getFollowingCount(me);
        var ops = redisTemplate.opsForValue();
        ops.increment(REDIS_FOLLOWER_COUNT_PREFIX + target);
        ops.increment(REDIS_FOLLOWING_COUNT_PREFIX + me);
        ops.getAndDelete(REDIS_FOLLOWER_HANDLES_PREFIX + target);
        ops.getAndDelete(REDIS_FOLLOWING_HANDLES_PREFIX + me);
        try {
            userNodeRepository.follow(me, target);
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean unfollow(String me, String target) {
        getFollowerCount(target);
        getFollowingCount(me);
        var ops = redisTemplate.opsForValue();
        Long followersOfTarget = ops.decrement(REDIS_FOLLOWER_COUNT_PREFIX + target);
        if(followersOfTarget != null && followersOfTarget < 0) {
            ops.increment(REDIS_FOLLOWER_COUNT_PREFIX + target);
        }
        Long followingsOfMe = ops.decrement(REDIS_FOLLOWING_COUNT_PREFIX + me);
        if(followingsOfMe != null && followingsOfMe < 0) {
            ops.increment(REDIS_FOLLOWING_COUNT_PREFIX + me);
        }
        ops.getAndDelete(REDIS_FOLLOWER_HANDLES_PREFIX + target);
        ops.getAndDelete(REDIS_FOLLOWING_HANDLES_PREFIX + me);
        try {
            userNodeRepository.unfollow(me, target);
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<String> getFollowerOf(String handle) {
        String handles = redisTemplate.opsForValue().get(REDIS_FOLLOWER_HANDLES_PREFIX + handle);
        if(handles != null) {
            return new ArrayList<>(Arrays.asList(handles.split(",")));
        }
        try {
            var followers = userNodeRepository.getAllFollowers(handle);
            StringBuilder followersStr = new StringBuilder();
            for(int i=0; i<followers.size(); i++) {
                followersStr.append(followers.get(i));
                if(i < followers.size() - 1)
                    followersStr.append(",");
            }
            redisTemplate.opsForValue()
                    .set(REDIS_FOLLOWER_HANDLES_PREFIX + handle, followersStr.toString(), 30L, TimeUnit.MINUTES);
            return followers;
        }catch (Exception e) {
            return null;
        }
    }

    public List<String> getFollowingOf(String handle) {
        String handles = redisTemplate.opsForValue().get(REDIS_FOLLOWING_HANDLES_PREFIX + handle);
        if(handles != null) {
            return new ArrayList<>(Arrays.asList(handles.split(",")));
        }
        try{
            var followings = userNodeRepository.getAllFollowings(handle);
            StringBuilder followingsStr = new StringBuilder();
            for(int i=0; i<followings.size(); i++) {
                followingsStr.append(followings.get(i));
                if(i < followings.size() - 1)
                    followingsStr.append(",");
            }
            redisTemplate.opsForValue()
                    .set(REDIS_FOLLOWING_HANDLES_PREFIX + handle, followingsStr.toString(), 30L, TimeUnit.MINUTES);
            return followings;
        }catch(Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    public boolean isFollowing(String me, String target) {
        return userNodeRepository.isFollowing(me, target);
    }

    public List<Boolean> isFollowingMany(String me, List<String> handles) {
        return userNodeRepository.isFollowingMany(me, handles);
    }

    public int getFollowerCount(String handle) {
        String followers = redisTemplate.opsForValue().get(REDIS_FOLLOWER_COUNT_PREFIX + handle);
        if(followers == null) {
            int count = userNodeRepository.getFollowerCount(handle);
            redisTemplate.opsForValue()
                    .set(REDIS_FOLLOWER_COUNT_PREFIX + handle, Integer.toString(count), 30L, TimeUnit.MINUTES);
            return count;
        }
        return Integer.parseInt(followers);
    }

    public int getFollowingCount(String handle) {
        String followings = redisTemplate.opsForValue().get(REDIS_FOLLOWING_COUNT_PREFIX + handle);
        if(followings == null) {
            int count = userNodeRepository.getFollowingCount(handle);
            redisTemplate.opsForValue()
                    .set(REDIS_FOLLOWING_COUNT_PREFIX + handle, Integer.toString(count), 30L, TimeUnit.MINUTES);
            return count;
        }
        return Integer.parseInt(followings);
    }

    public void clearAllCache(String handle) {
        var ops = redisTemplate.opsForValue();
        ops.getAndDelete(REDIS_FOLLOWER_COUNT_PREFIX + handle);
        ops.getAndDelete(REDIS_FOLLOWING_COUNT_PREFIX+ handle);
        ops.getAndDelete(REDIS_FOLLOWER_HANDLES_PREFIX + handle);
        ops.getAndDelete(REDIS_FOLLOWING_HANDLES_PREFIX + handle);
    }
}
