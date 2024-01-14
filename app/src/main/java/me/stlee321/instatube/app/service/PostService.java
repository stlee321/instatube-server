package me.stlee321.instatube.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.req.PostForm;
import me.stlee321.instatube.app.distlock.DistLock;
import me.stlee321.instatube.app.entity.post.Post;
import me.stlee321.instatube.app.entity.post.PostRepository;
import me.stlee321.instatube.app.service.dto.PostDetail;
import me.stlee321.instatube.app.service.dto.PostInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    private final ImageService imageService;
    private final FollowService followService;
    private final PostRankService postRankService;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    private static final String POST_CACHE_KEY = "post:cache:";
    private static final String POST_USER_COUNT = "post:count:";

    public PostService(
            PostRepository postRepository,
            ImageService imageService,
            FollowService followService,
            PostRankService postRankService,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.postRepository = postRepository;
        this.imageService = imageService;
        this.followService = followService;
        this.postRankService = postRankService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    public List<PostInfo> getRecommendedForAnon(Long timestamp, Integer size) {
        List<Post> posts = postRepository.getPostsBefore(timestamp, Pageable.ofSize(size));
        return posts.stream().map(post -> PostInfo.builder()
                .postId(post.getPostId())
                .timestamp(post.getTimestamp())
                .build()
        ).sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).toList();
    }

    public List<PostInfo> getRecommendedFor(String handle, Long timestamp, Integer size) {
        // 개인화된 포스트 추천 미구현.
        // getRecommendedForAnon과 같음
        List<Post> posts = postRepository.getPostsBefore(timestamp, Pageable.ofSize(size));
        return posts.stream().map(post -> PostInfo.builder()
                .postId(post.getPostId())
                .timestamp(post.getTimestamp())
                .build()
        ).sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).toList();
    }

    public List<PostInfo> getFollowingPostsBefore(Long timestamp, String handle, int size) {
        // 매우 비효율적인 구현
        // Feed Cache로 구현하는 것이 효율적
        List<String> followings = getFollowingHandles(handle);
        List<PostInfo> postInfos = new ArrayList<>();
        for(String following : followings) {
            List<Post> posts = postRepository.getUserPostsBefore(timestamp, following, Pageable.ofSize(size));
            posts.forEach(p -> {
                postInfos.add(
                        PostInfo.builder()
                                .postId(p.getPostId())
                                .timestamp(p.getTimestamp())
                                .build()
                );
            });
        }
        return postInfos.stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).toList();
    }


    public List<PostInfo> getFollowingPostsAfter(Long timestamp, String handle, int size) {
        // 매우 비효율적인 구현
        // Feed Cache로 구현하는 것이 효율적
        List<String> followings = getFollowingHandles(handle);
        List<PostInfo> postInfos = new ArrayList<>();
        for(String following : followings) {
            List<Post> posts = postRepository.getUserPostsAfter(timestamp, following, Pageable.ofSize(size));
            posts.forEach(p -> {
                postInfos.add(
                        PostInfo.builder()
                                .postId(p.getPostId())
                                .timestamp(p.getTimestamp())
                                .build()
                );
            });
        }
        return postInfos.stream().sorted((a,b)->b.getTimestamp().compareTo(a.getTimestamp())).toList();
    }

    public List<PostInfo> getPostsBefore(Long timestamp, String handle, int size) {
        List<Post> posts = postRepository.getUserPostsBefore(timestamp, handle, Pageable.ofSize(size));
        return posts.stream().map(p -> PostInfo.builder()
                .postId(p.getPostId())
                .timestamp(p.getTimestamp())
                .build()).toList();
    }

    public List<PostInfo> getPostsAfter(Long timestamp, String handle, int size) {
        List<Post> posts = postRepository.getUserPostsAfter(timestamp, handle, Pageable.ofSize(size));
        return posts.stream().map(p -> PostInfo.builder()
                .postId(p.getPostId())
                .timestamp(p.getTimestamp())
                .build()).toList();
    }

    public PostDetail getPost(String postId) {
        String cached = redisTemplate.opsForValue().get(POST_CACHE_KEY + postId);
        if(cached != null) {
            try {
                Post cachedPost = objectMapper.readValue(cached, Post.class);
                return PostDetail.fromPost(cachedPost);
            }catch(Exception e) {
                log.info("object mapper error while reading " + cached);
            }
        }
        var postOptional = postRepository.findByPostId(postId);
        if(postOptional.isPresent()) {
            Post post = postOptional.get();
            try {
                String postString = objectMapper.writeValueAsString(post);
                redisTemplate.opsForValue().set(POST_CACHE_KEY + postId, postString, 12L, TimeUnit.HOURS);
            }catch(Exception e) {
                log.info("object mapper error writing " + postId);
                log.info(e.toString());
            }
            return PostDetail.fromPost(post);
        }
        return null;
    }

    @DistLock(keyName = "post")
    public PostDetail createPost(String handle, PostForm postForm) {
        Post newPost = Post.builder()
                .postId(generateId())
                .handle(handle)
                .title(postForm.getTitle())
                .content(postForm.getContent())
                .imageId(postForm.getImageId())
                .build();
        try {
            postRepository.save(newPost);
            redisTemplate.opsForValue().getAndDelete(POST_USER_COUNT + newPost.getHandle());
        }catch(Exception e) {
            return null;
        }
        return PostDetail.fromPost(newPost);
    }

    @DistLock(keyName = "post")
    public PostDetail updatePost(PostDetail postDetail) {
        var postOptional = postRepository.findByPostId(postDetail.getPostId());
        if(postOptional.isPresent()) {
            Post post = postOptional.get();
            if(!post.getHandle().equals(postDetail.getHandle())) {
                return null;
            }
            post.setTitle(postDetail.getTitle());
            post.setContent(postDetail.getContent());
            post.setImageId(postDetail.getImageId());
            try {
                Post updated = postRepository.save(post);
                redisTemplate.opsForValue().getAndDelete(POST_CACHE_KEY + updated.getPostId());
                return PostDetail.fromPost(updated);
            }catch(Exception e) {
                return null;
            }
        }
        return null;
    }

    @DistLock(keyName = "post")
    public boolean deletePost(String handle, String postId) {
        var postOptional = postRepository.findByPostId(postId);
        if(postOptional.isPresent()) {
            Post post = postOptional.get();
            if(!post.getHandle().equals(handle)) {
                return false;
            }
            try {
                postRepository.deleteById(post.getId());
                redisTemplate.opsForValue().getAndDelete(POST_CACHE_KEY + postId);
                redisTemplate.opsForValue().getAndDelete(POST_USER_COUNT + post.getHandle());
                return true;
            }catch(Exception e) {
                return false;
            }
        }
        return false;
    }

    public String generateId() {
        final String alphanumeric = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        long left = new Random().nextLong(Long.MAX_VALUE);
        long right = new Random().nextLong(Long.MAX_VALUE);
        StringBuilder id = new StringBuilder();
        for(int i=0;i<5;i++) {
            int idx = (int)(left % 62);
            id.append(alphanumeric.charAt(idx));
            left /= 64;
        }
        for(int i=0;i<6;i++) {
            int idx = (int)(right % 62);
            id.append(alphanumeric.charAt(idx));
            right /= 64;
        }
        return id.toString();
    }
    public List<String> getFollowingHandles(String handle) {
        return followService.getFollowingOf(handle);
    }

    public Long getUserPostCount(String handle) {
        String cached = redisTemplate.opsForValue().get(POST_USER_COUNT + handle);
        if(cached != null) {
            try {
                return Long.parseLong(cached);
            }catch(NumberFormatException e) {
                log.info("Exception while parsing Long string");
                log.info(e.toString());
            }
        }
        Long count = postRepository.countByHandle(handle);
        redisTemplate.opsForValue().set(POST_USER_COUNT + handle, count.toString(), 12L, TimeUnit.HOURS);
        return count;
    }
    @DistLock(keyName = "post")
    public void deleteAllPostsOf(String handle) {
        redisTemplate.opsForValue().getAndDelete(POST_USER_COUNT + handle);
        List<Post> allPosts = postRepository.findByHandle(handle);
        allPosts.forEach(p -> {
            redisTemplate.opsForValue().getAndDelete(POST_CACHE_KEY + p.getPostId());
            postRankService.removePost(p.getPostId());
        });
        postRepository.deleteByHandle(handle);
    }
}
