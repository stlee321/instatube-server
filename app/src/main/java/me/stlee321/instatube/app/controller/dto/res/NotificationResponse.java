package me.stlee321.instatube.app.controller.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.service.dto.NotificationItem;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationResponse {
    public NotificationResponse() {}

    private String from;
    private String target;
    private String type;
    private String link;
    private Long timestamp;

    public static NotificationResponse fromNotificationItem(NotificationItem notification) {
        return NotificationResponse.builder()
                .from(notification.getFrom())
                .target(notification.getTarget())
                .type(notification.getType())
                .link(notification.getLink())
                .timestamp(notification.getTimestamp())
                .build();
    }

}
