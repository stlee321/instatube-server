package me.stlee321.instatube.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.req.PostForm;
import me.stlee321.instatube.app.controller.dto.res.PostResponse;
import me.stlee321.instatube.app.service.AuthService;
import me.stlee321.instatube.app.service.PostRankService;
import me.stlee321.instatube.app.service.PostService;
import me.stlee321.instatube.app.service.dto.PostDetail;
import me.stlee321.instatube.app.service.dto.PostInfo;
import me.stlee321.instatube.app.service.dto.UserInfo;
import me.stlee321.instatube.app.util.PageRequestDirection;
import me.stlee321.instatube.app.validator.handle.Handle;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@Validated
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostRankService postRankService;
    private final AuthService authService;

    public PostController(
            PostService postService,
            PostRankService postRankService,
            AuthService authService
    ) {
        this.postService = postService;
        this.postRankService = postRankService;
        this.authService = authService;
    }
    @GetMapping("/recommend")
    public List<PostInfo> getRecommendedPosts(
            @RequestParam("timestamp") Long timestamp,
            @RequestParam(value = "size", required = false, defaultValue = "12") Integer size,
            Authentication authentication
    ) {
        if(authentication == null) {
            return postService.getRecommendedForAnon(timestamp, size);
        }
        String handle = authentication.getName();
        return postService.getRecommendedFor(handle, timestamp, size);
    }

    @GetMapping("/following")
    public List<PostInfo> getFollowingPosts(
            @RequestParam("dir")PageRequestDirection dir,
            @Min(0) @RequestParam("timestamp") Long timestamp,
            @Min(1) @RequestParam(value = "size", defaultValue = "12") Integer size,
            Authentication authentication
    ) {
        if(authentication == null) {
            return null;
        }
        String handle = authentication.getName();
        List<PostInfo> posts = null;
        if(dir == PageRequestDirection.BEFORE) {
            posts = postService.getFollowingPostsBefore(timestamp, handle, size);
        }else if(dir == PageRequestDirection.AFTER) {
            posts = postService.getFollowingPostsAfter(timestamp, handle, size);
        }
        return posts;
    }

    @GetMapping("/popular")
    public List<String> getPopularPosts(@RequestParam(value = "size", required = false, defaultValue = "6") Long size) {
        return postRankService.getPopularPosts(size);
    }

    @GetMapping("/u/{handle}")
    public List<PostInfo> getUserPosts(
            @Handle @PathVariable("handle") String handle,
            @RequestParam("dir") PageRequestDirection dir,
            @Min(0) @RequestParam("timestamp") Long timestamp,
            @Min(1) @RequestParam(value = "size", defaultValue = "12") Integer size
            ) {
        List<PostInfo> posts = null;
        if(dir == PageRequestDirection.BEFORE) {
            posts = postService.getPostsBefore(timestamp, handle, size);
        }else if(dir == PageRequestDirection.AFTER) {
            posts = postService.getPostsAfter(timestamp, handle, size);
        }
        return posts;
    }

    @GetMapping("/u/count/{handle}")
    public ResponseEntity<Long> getUserPostCount(
            @Handle @PathVariable("handle") String handle
    ) {
        UserInfo user = authService.getUserInfo(handle);
        if(user == null) {
            return ResponseEntity.notFound().build();
        }
        long count = postService.getUserPostCount(handle);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/p/")
    public ResponseEntity emptyPostId() {
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/p/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable("postId") String postId,
            @RequestParam(value = "tr", required = false) Integer tracking
    ) {
        PostDetail postDetail = postService.getPost(postId);
        if(postDetail == null) {
            return ResponseEntity.notFound().build();
        }
        if(tracking != null)
            postRankService.postViewed(postId);
        return ResponseEntity.ok().body(PostResponse.fromPostDetail(postDetail));
    }

    @PostMapping("")
    public ResponseEntity<PostResponse> writePost(
            @Valid @RequestBody PostForm postForm,
            Authentication authentication
    ) throws Exception {
        if(authentication == null) {
            return ResponseEntity.badRequest().body(null);
        }
        String handle = authentication.getName();
        PostDetail postDetail = postService.createPost(handle, postForm);
        if(postDetail == null) {
            return ResponseEntity.internalServerError().body(null);
        }
        postRankService.postCreated(postDetail.getPostId());
        URI uri = new URI("/api/post/p/" + postDetail.getPostId());
        return ResponseEntity.created(uri).body(PostResponse.fromPostDetail(postDetail));
    }

    @PatchMapping("/p/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @Valid @RequestBody PostForm form,
            @PathVariable("postId") String postId,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.badRequest().build();
        }
        String handle = authentication.getName();
        PostDetail postDetail = postService.getPost(postId);
        if(postDetail == null) {
            return ResponseEntity.badRequest().build();
        }
        if(!postDetail.getHandle().equals(handle)) {
            return ResponseEntity.badRequest().build();
        }
        postDetail.setTitle(form.getTitle());
        postDetail.setContent(form.getContent());
        postDetail.setImageId(form.getImageId());
        postDetail = postService.updatePost(postDetail);
        if(postDetail == null) {
            return ResponseEntity.internalServerError().body(null);
        }
        return ResponseEntity.ok().body(PostResponse.fromPostDetail(postDetail));
    }

    @DeleteMapping("/p/{postId}")
    public ResponseEntity deletePost(@PathVariable("postId") String postId,  Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.badRequest().build();
        }
        String handle = authentication.getName();
        boolean result = postService.deletePost(handle, postId);
        if(result) {
            return ResponseEntity.noContent().build();
        }
        postRankService.removePost(postId);
        return ResponseEntity.badRequest().build();
    }
}
