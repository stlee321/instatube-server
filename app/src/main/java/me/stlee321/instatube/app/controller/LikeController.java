package me.stlee321.instatube.app.controller;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.res.LikeResponse;
import me.stlee321.instatube.app.service.LikeService;
import me.stlee321.instatube.app.service.NotificationService;
import me.stlee321.instatube.app.service.PostRankService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
@Slf4j
public class LikeController {

    private final LikeService likeService;
    private final PostRankService postRankService;

    private final NotificationService notificationService;

    public LikeController(
            LikeService likeService,
            PostRankService postRankService,
            NotificationService notificationService
    ) {
        this.likeService = likeService;
        this.postRankService = postRankService;
        this.notificationService = notificationService;
    }
    @GetMapping("/count/p/{postId}")
    public ResponseEntity<LikeResponse> getPostLikes(@PathVariable("postId") String postId) {
        int likes = likeService.getPostLikes(postId);
        return ResponseEntity.ok(LikeResponse.builder()
                        .message("total number of likes of post " + postId)
                        .count(likes)
                        .result(true)
                .build());
    }

    @GetMapping("/count/r/{replyId}")
    public ResponseEntity<LikeResponse> getReplyLikes(@PathVariable("replyId") String replyId) {
        int likes = likeService.getReplyLikes(replyId);
        return ResponseEntity.ok(LikeResponse.builder()
                        .message("total number of likes of reply " + replyId)
                        .count(likes)
                        .result(true)
                .build());
    }

    @GetMapping("/p/{postId}")
    public ResponseEntity<LikeResponse> isPostLikedBy(@PathVariable("postId") String postId, @RequestParam("handle") String handle) {
        boolean liked = likeService.isPostLikedBy(postId, handle);
        return ResponseEntity.ok(LikeResponse.builder()
                        .message("is post " + postId + " liked by " + handle)
                        .result(liked)
                .build());
    }

    @GetMapping("/r/{replyId}")
    public ResponseEntity<LikeResponse> isReplyLikedBy(@PathVariable("replyId") String replyId, @RequestParam("handle") String handle) {
        boolean liked = likeService.isReplyLikedBy(replyId, handle);
        return ResponseEntity.ok(LikeResponse.builder()
                        .message("is reply " + replyId + " liked by " + handle)
                        .result(liked)
                .build());
    }

    @PostMapping("/p/{postId}")
    public ResponseEntity<LikeResponse> likePost(@PathVariable("postId") String postId, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = likeService.likePost(postId, me);
        if(result) {
            notificationService.publishPostLikeNotification(me, postId);
            postRankService.postLiked(postId);
            return ResponseEntity.ok(LikeResponse.builder()
                            .message(me + " likes post " + postId)
                            .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(LikeResponse.builder()
                        .message("something gone wrong")
                        .result(false)
                        .build());
    }

    @DeleteMapping("/p/{postId}")
    public ResponseEntity<LikeResponse> unlikePost(@PathVariable("postId") String postId, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = likeService.unlikePost(postId, me);
        if(result) {
            return ResponseEntity.ok(LikeResponse.builder()
                    .message(me + " unlikes post " + postId)
                    .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(LikeResponse.builder()
                        .message("something gone wrong")
                        .result(false)
                        .build());
    }

    @PostMapping("/r/{replyId}")
    public ResponseEntity<LikeResponse> likeReply(@PathVariable("replyId") String replyId, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = likeService.likeReply(replyId, me);
        if(result) {
            notificationService.publishReplyLikeNotification(me, replyId);
            return ResponseEntity.ok(LikeResponse.builder()
                    .message(me + " likes reply " + replyId)
                    .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(LikeResponse.builder()
                        .message("something gone wrong")
                        .result(false)
                        .build());
    }

    @DeleteMapping("/r/{replyId}")
    public ResponseEntity<LikeResponse> unlikeReply(@PathVariable("replyId") String replyId, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = likeService.unlikeReply(replyId, me);
        if(result) {
            return ResponseEntity.ok(LikeResponse.builder()
                    .message(me + " unlikes reply " + replyId)
                    .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(LikeResponse.builder()
                        .message("something gone wrong")
                        .result(false)
                        .build());
    }
}
