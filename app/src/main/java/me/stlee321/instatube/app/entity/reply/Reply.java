package me.stlee321.instatube.app.entity.reply;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.entity.BaseEntity;
import me.stlee321.instatube.app.util.MicroTimestamp;

@Entity
@Table(name = "reply")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Reply extends BaseEntity {
    public Reply() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reply_id", nullable = false, updatable = false)
    private String replyId;
    @Column(name = "handle", nullable = false, updatable = false)
    private String handle;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "post_id", nullable = false, updatable = false)
    private String postId;
    @Column(name = "target_id", updatable = false)
    private String targetId;
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Long timestamp;

    @PrePersist
    public void setTimestamp() {
        timestamp = MicroTimestamp.fromLocalDateTime(getCreatedAt());
    }
}
