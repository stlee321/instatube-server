package me.stlee321.instatube.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.entity.post.Post;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostDetail {
    public PostDetail() {}
    private String postId;
    private String title;
    private String handle;
    private String content;
    private String imageId;
    private LocalDateTime createdAt;
    private Long timestamp;

    public static PostDetail fromPost(Post post) {
        return PostDetail.builder()
                .postId(post.getPostId())
                .handle(post.getHandle())
                .title(post.getTitle())
                .content(post.getContent())
                .imageId(post.getImageId())
                .createdAt(post.getCreatedAt())
                .timestamp(post.getTimestamp())
                .build();
    }
}
