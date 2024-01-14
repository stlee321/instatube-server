package me.stlee321.instatube.app.service;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.res.NotificationResponse;
import me.stlee321.instatube.app.service.dto.Notification;
import me.stlee321.instatube.app.service.dto.NotificationItem;
import me.stlee321.instatube.app.service.dto.PostDetail;
import me.stlee321.instatube.app.service.dto.ReplyDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {
    private final SqsTemplate sqsTemplate;
    private final DynamoDbTemplate dynamoDbTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final PostService postService;
    private final ReplyService replyService;
    private final ConcurrentHashMap<String, SseEmitter> emitterStorage = new ConcurrentHashMap<>();
    private static final Long SSE_TIMEOUT = 20 * 60 * 1000L;
    private static final String QUEUE_NAME = "instatube-notification";
    private static final String UNSENT_NOTIFICATIONS_KEY = "unsentNotifications";



    @Getter
    enum NotificationEventName {
        CONNECTED("CONNECTED"),
        NEW_NOTIFICATION("NEWNOTI");

        private final String eventName;
        NotificationEventName(String eventName) {
            this.eventName = eventName;
        }
    }

    @Getter
    enum NotificationType {
        LIKE_POST("LIKE_POST"), LIKE_REPLY("LIKE_REPLY"),
        FOLLOW("FOLLOW"), REPLY_POST("REPLY_POST"), REPLY_REPLY("REPLY_REPLY"),
        PWD_CHANGED("PWD_CHANGED");
        private final String type;
        NotificationType(String type) {
            this.type = type;
        }
    }
    public NotificationService(
            SqsTemplate sqsTemplate,
            DynamoDbTemplate dynamoDbTemplate,
            RedisTemplate<String, String> redisTemplate,
            PostService postService,
            ReplyService replyService
    ) {
        this.sqsTemplate = sqsTemplate;
        this.dynamoDbTemplate = dynamoDbTemplate;
        this.redisTemplate = redisTemplate;
        this.postService = postService;
        this.replyService = replyService;
    }

    public SseEmitter subscribe(String handle) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.onCompletion(() -> {
            emitterStorage.remove(handle);
        });
        emitter.onTimeout(() -> {
            emitterStorage.remove(handle);
        });
        emitterStorage.put(handle, emitter);
        sendWelcomeNotification(handle);
        // 아직 읽지 않은 알람 있을때
        if(checkUnsentNotifications(handle)) {
            sendNewNotificationAlarm(handle);
        }
        return emitter;
    }

    private void sendWelcomeNotification(String handle) {
        SseEmitter emitter = emitterStorage.get(handle);
        if(emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .id(handle + "_" + System.currentTimeMillis())
                    .name(NotificationEventName.CONNECTED.getEventName())
                    .data("sse connected"));
        }catch(Exception e) {
            log.info(e.toString());
        }
    }

    private void sendNewNotificationAlarm(String handle) {
        SseEmitter emitter = emitterStorage.get(handle);
        if(emitter == null) {
            markUnsentNotifications(handle);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .id(handle + "_" + System.currentTimeMillis())
                    .name(NotificationEventName.NEW_NOTIFICATION.getEventName())
                    .data("new notifications exist"));
            unmarkUnsentNotifications(handle);
        }catch(Exception e) {
            log.info(e.toString());
        }
    }
    public void publishPostLikeNotification(String liker, String postId) {
        PostDetail post = postService.getPost(postId);
        if(post == null) return;
        if(post.getHandle().equals(liker)) return;
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from(liker)
                .target(post.getHandle())
                .type(NotificationType.LIKE_POST.getType())
                .link("/p/" + post.getPostId())
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }
    public void publishReplyLikeNotification(String liker, String replyId) {
        ReplyDetail reply = replyService.getReply(replyId);
        if(reply == null) return;
        if(reply.getHandle().equals(liker)) return;
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from(liker)
                .target(reply.getHandle())
                .type(NotificationType.LIKE_REPLY.getType())
                .link("/p/" + reply.getPostId())
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }
    public void publishFollowNotification(String from, String to) {
        if(from.equals(to)) return;
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from(from)
                .target(to)
                .type(NotificationType.FOLLOW.getType())
                .link("/u/" + from)
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }
    public void publishPostReplyNotification(String replier, String postId) {
        PostDetail post = postService.getPost(postId);
        if(post == null) return;
        if(post.getHandle().equals(replier)) return;
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from(replier)
                .target(post.getHandle())
                .type(NotificationType.REPLY_POST.getType())
                .link("/p/" + post.getPostId())
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }

    public void publishReplyReplyNotification(String replier, String targetId) {
        ReplyDetail reply = replyService.getReply(targetId);
        if(reply == null) return;
        if(reply.getHandle().equals(replier)) return;
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from(replier)
                .target(reply.getHandle())
                .type(NotificationType.REPLY_REPLY.getType())
                .link("/p/" + reply.getPostId())
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }

    public void publishAlertResetPassword(String handle) {
        Date now = new Date();
        Notification newNotification = Notification.builder()
                .from("")
                .target(handle)
                .type(NotificationType.PWD_CHANGED.getType())
                .link("")
                .timestamp(now.getTime()).build();
        var message = MessageBuilder.withPayload(newNotification).build();
        sqsTemplate.send(QUEUE_NAME, message);
    }

    private void saveNotification(Notification notification) {
        dynamoDbTemplate.save(NotificationItem.fromNotification(notification));
    }

    private void removeOldNotifications(String handle, Long before) {
        var results = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional
                        .sortLessThan(Key.builder()
                                .partitionValue("notification#" + handle)
                                .sortValue(before).build())).build(), NotificationItem.class);
        results.items().forEach(dynamoDbTemplate::delete);
    }

    public List<NotificationResponse> getNotifications(
            String handle, String direction, Long timestamp, Integer size) {
        if(direction.equals("after")) {
            var results = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional
                            .sortGreaterThanOrEqualTo(Key.builder()
                                    .partitionValue("notification#" + handle)
                                    .sortValue(timestamp).build()))
                    .limit(size).build(), NotificationItem.class);
            return results.items().stream()
                    .map(NotificationResponse::fromNotificationItem).toList();
        }else if(direction.equals("before")) {
            var results = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                    .queryConditional(QueryConditional
                            .sortLessThanOrEqualTo(Key.builder()
                                    .partitionValue("notification#" + handle)
                                    .sortValue(timestamp).build()))
                    .scanIndexForward(false)
                    .limit(size).build(), NotificationItem.class);
            return results.items().stream()
                    .map(NotificationResponse::fromNotificationItem).toList();
        }
        return null;
    }

    @SqsListener({QUEUE_NAME})
    void listenNotifications(Notification notification) {
        saveNotification(notification);
        sendNewNotificationAlarm(notification.getTarget());
    }

    private boolean checkUnsentNotifications(String handle) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(UNSENT_NOTIFICATIONS_KEY, handle));
    }
    private void markUnsentNotifications(String handle) {
        redisTemplate.opsForSet().add(UNSENT_NOTIFICATIONS_KEY, handle);
    }
    private void unmarkUnsentNotifications(String handle) {
        redisTemplate.opsForSet().remove(UNSENT_NOTIFICATIONS_KEY, handle);
    }
}
