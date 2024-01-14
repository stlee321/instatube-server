package me.stlee321.instatube.app.controller.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.service.dto.PostDetail;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostResponse {
    public PostResponse() {}
    private String postId;
    private String title;
    private String handle;
    private String content;
    private String imageId;
    private LocalDateTime createdAt;
    private Long timestamp;

    public static PostResponse fromPostDetail(PostDetail postDetail) {
        return PostResponse.builder()
                .postId(postDetail.getPostId())
                .title(postDetail.getTitle())
                .handle(postDetail.getHandle())
                .content(postDetail.getContent())
                .imageId(postDetail.getImageId())
                .createdAt(postDetail.getCreatedAt())
                .timestamp(postDetail.getTimestamp())
                .build();
    }
}
