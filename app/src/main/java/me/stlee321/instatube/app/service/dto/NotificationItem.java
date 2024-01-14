package me.stlee321.instatube.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Builder
@Getter
@Setter
@AllArgsConstructor
@DynamoDbBean
public class NotificationItem implements Comparable<NotificationItem>{
    public NotificationItem() {}
    private String from;
    private String target;
    private String type;
    private String link;
    private Long timestamp;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("target")
    public String getTarget() {
        return "notification#" + target;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    public static NotificationItem fromNotification(Notification notification) {
        return NotificationItem.builder()
                .from(notification.getFrom())
                .target(notification.getTarget())
                .type(notification.getType())
                .link(notification.getLink())
                .timestamp(notification.getTimestamp())
                .build();
    }

    @Override
    public int compareTo(NotificationItem o) {
        if(this.target.equals(o.getTarget())) {
            return this.timestamp.compareTo(o.getTimestamp());
        }
        return this.target.compareTo(o.getTarget());
    }
}
