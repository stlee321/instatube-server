package me.stlee321.instatube.app.controller.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.service.dto.ReplyDetail;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReplyResponse {
    public ReplyResponse() {}
    private String replyId;
    private String postId;
    private String handle;
    private String content;
    private LocalDateTime createdAt;
    private Long timestamp;
    public static ReplyResponse fromReplyDetail(ReplyDetail replyDetail) {
        return ReplyResponse.builder()
                .replyId(replyDetail.getReplyId())
                .postId(replyDetail.getPostId())
                .handle(replyDetail.getHandle())
                .content(replyDetail.getContent())
                .createdAt(replyDetail.getCreatedAt())
                .timestamp(replyDetail.getTimestamp())
                .build();
    }
}
