package me.stlee321.instatube.app.entity.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.entity.BaseEntity;
import me.stlee321.instatube.app.util.MicroTimestamp;

@Entity
@Table(name = "post")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    public Post() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_id", unique = true, nullable = false)
    private String postId;
    @Column(name = "handle", nullable = false)
    private String handle;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "content")
    private String content;
    @Column(name = "image_id")
    private String imageId;
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @PrePersist
    public void setTimestamp() {
        timestamp = MicroTimestamp.fromLocalDateTime(getCreatedAt());
    }
}
