package me.stlee321.instatube.app.controller;

import me.stlee321.instatube.app.controller.dto.res.FollowResponse;
import me.stlee321.instatube.app.service.FollowService;
import me.stlee321.instatube.app.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;
    private final NotificationService notificationService;
    public FollowController(
            FollowService followService,
            NotificationService notificationService
    ) {
        this.followService = followService;
        this.notificationService = notificationService;
    }
    @PostMapping("/{handle}")
    public ResponseEntity<FollowResponse> follow(@PathVariable("handle") String target, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = followService.follow(me, target);
        if(result) {
            notificationService.publishFollowNotification(me, target);
            return ResponseEntity.ok(FollowResponse.builder()
                            .message(me + " follows " + target)
                            .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(FollowResponse.builder()
                        .message("something wrong")
                        .result(false)
                        .build());
    }

    @DeleteMapping("/{handle}")
    public ResponseEntity<FollowResponse> unfollow(@PathVariable("handle") String target, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = followService.unfollow(me, target);
        if(result) {
            return ResponseEntity.ok(FollowResponse.builder()
                    .message(me + " doesn't follow " + target)
                    .result(true)
                    .build());
        }
        return ResponseEntity.internalServerError()
                .body(FollowResponse.builder()
                        .message("something wrong")
                        .result(false)
                        .build());
    }

    @GetMapping("/follower/{handle}")
    public ResponseEntity<FollowResponse> getFollowerOf(@PathVariable("handle") String handle) {
        List<String> followers = followService.getFollowerOf(handle);
        if(followers == null) {
            return ResponseEntity.internalServerError().body(FollowResponse.builder()
                            .message("something wrong")
                            .result(false)
                    .build());
        }
        String message = String.join(",", followers);
        return ResponseEntity.ok(FollowResponse.builder()
                        .message(message)
                        .result(true)
                .build());
    }

    @GetMapping("/follower/count/{handle}")
    public ResponseEntity<FollowResponse> getFollowerCount(@PathVariable("handle") String handle) {
        int count = followService.getFollowerCount(handle);
        return ResponseEntity.ok(FollowResponse.builder()
                        .message("total number of followers of " + handle)
                        .count(count)
                        .result(true)
                .build());
    }

    @GetMapping("/following/{handle}")
    public ResponseEntity<FollowResponse> getFollowingOf(@PathVariable("handle") String handle) {
        List<String> followings = followService.getFollowingOf(handle);
        if(followings == null) {
            return ResponseEntity.internalServerError().body(FollowResponse.builder()
                    .message("something wrong")
                    .result(false)
                    .build());
        }
        String message = String.join(",", followings);
        return ResponseEntity.ok(FollowResponse.builder()
                .message(message)
                .result(true)
                .build());
    }

    @GetMapping("/following/count/{handle}")
    public ResponseEntity<FollowResponse> getFollowingCount(@PathVariable("handle") String handle) {
        int count = followService.getFollowingCount(handle);
        return ResponseEntity.ok(FollowResponse.builder()
                .message("total number of followings of " + handle)
                .count(count)
                .result(true)
                .build());
    }

    @GetMapping("/is/following/{handle}")
    public ResponseEntity<FollowResponse> isFollowing(@PathVariable("handle") String target, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        boolean result = followService.isFollowing(me, target);
        return ResponseEntity.ok(FollowResponse.builder()
                        .message("is following " + target)
                        .result(result)
                .build());
    }

    @GetMapping("/is/following")
    public ResponseEntity<FollowResponse> isFollowingMany(@RequestParam("handles")List<String> handles, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String me = authentication.getName();
        List<Boolean> result = followService.isFollowingMany(me, handles);
        String message = String.join(",", result.stream().map(ret -> ret ? "true" : "false").toList());
        return ResponseEntity.ok(FollowResponse.builder()
                        .message(message)
                        .result(true)
                .build());
    }
}
