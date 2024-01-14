package me.stlee321.instatube.app.controller;

import jakarta.validation.Valid;
import me.stlee321.instatube.app.controller.dto.req.ReplyForm;
import me.stlee321.instatube.app.controller.dto.req.ReplyUpdateForm;
import me.stlee321.instatube.app.controller.dto.res.ReplyResponse;
import me.stlee321.instatube.app.service.NotificationService;
import me.stlee321.instatube.app.service.PostRankService;
import me.stlee321.instatube.app.service.ReplyService;
import me.stlee321.instatube.app.service.dto.ReplyDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reply")
public class ReplyController {

    private final ReplyService replyService;
    private final PostRankService postRankService;
    private final NotificationService notificationService;

    public ReplyController(
            ReplyService replyService,
            PostRankService postRankService,
            NotificationService notificationService
    ) {
        this.replyService = replyService;
        this.postRankService = postRankService;
        this.notificationService = notificationService;
    }
    @GetMapping("")
    public ResponseEntity<List<ReplyResponse>> getReply(
            @RequestParam("timestamp") Long timestamp,
            @RequestParam("postId") String postId,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "targetId", required = false) String targetId
    ) {
        List<ReplyDetail> replies =
                replyService.getRepliesAfter(timestamp, postId, targetId, size);
        if(replies == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .body(replies.stream().map(ReplyResponse::fromReplyDetail).toList());
    }

    @PostMapping("")
    public ResponseEntity<ReplyResponse> createReply(
            @Valid @RequestBody ReplyForm form, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        ReplyDetail replyDetail = replyService.createReply(handle, form);
        if(replyDetail == null) {
            return ResponseEntity.internalServerError().build();
        }
        postRankService.postCommented(form.getPostId());
        notificationService.publishPostReplyNotification(handle, replyDetail.getPostId());
        if(replyDetail.getTargetId() != null && !replyDetail.getTargetId().isEmpty()) {
            notificationService.publishReplyReplyNotification(handle, replyDetail.getTargetId());
        }
        return ResponseEntity.ok()
                .body(ReplyResponse.fromReplyDetail(replyDetail));
    }

    @PatchMapping("/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @PathVariable("replyId") String replyId,
            @Valid @RequestBody ReplyUpdateForm form,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        if(!replyService.replyExists(replyId)) {
            return ResponseEntity.notFound().build();
        }
        if(!replyService.isReplyOwnedBy(handle, replyId)) {
            return ResponseEntity.badRequest().build();
        }
        ReplyDetail replyDetail = replyService.updateReply(handle, replyId, form);
        if(replyDetail == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .body(ReplyResponse.fromReplyDetail(replyDetail));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<String> deleteReply(
            @PathVariable("replyId") String replyId,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean replyExist = replyService.replyExists(replyId);
        if(!replyExist) {
            return ResponseEntity.notFound().build();
        }
        String handle = authentication.getName();
        boolean isOwner = replyService.isReplyOwnedBy(handle, replyId);
        if(!isOwner) return ResponseEntity.badRequest().build();
        replyService.deleteReply(handle, replyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/p/{postId}")
    public int getReplyCountOfPost(@PathVariable("postId") String postId) throws Exception {
        return replyService.getReplyCountOfPost(postId);
    }
    @GetMapping("/count/r/{replyId}")
    public int getReplyCountOfReply(@PathVariable("replyId") String replyId) throws Exception {
        return replyService.getReplyCountOfReply(replyId);
    }
}
