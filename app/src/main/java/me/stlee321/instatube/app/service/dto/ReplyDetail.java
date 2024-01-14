package me.stlee321.instatube.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.entity.reply.Reply;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReplyDetail implements Comparable<ReplyDetail> {
    public ReplyDetail() {}
    private String replyId;
    @Builder.Default
    private String targetId = "";
    private String postId;
    private String handle;
    private String content;
    private LocalDateTime createdAt;
    private Long timestamp;

    public static ReplyDetail fromReply(Reply reply) {
        return ReplyDetail.builder()
                .replyId(reply.getReplyId())
                .postId(reply.getPostId())
                .handle(reply.getHandle())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .timestamp(reply.getTimestamp())
                .targetId(reply.getTargetId())
                .build();
    }

    @Override
    public int compareTo(ReplyDetail o) {
        return o.getTimestamp().compareTo(timestamp);
    }
}
