package me.stlee321.instatube.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostInfo implements Comparable<PostInfo> {
    public PostInfo() {}
    private String postId;
    private Long timestamp;

    @Override
    public int compareTo(PostInfo o) {
        return o.getTimestamp().compareTo(timestamp);
    }
}
