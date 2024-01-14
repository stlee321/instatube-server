package me.stlee321.instatube.app.controller;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.res.NotificationResponse;
import me.stlee321.instatube.app.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/noti")
@Slf4j
public class NotificationController {


    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectSse(Authentication authentication) {
        if(authentication == null) {
            return null;
        }
        String handle = authentication.getName();
        SseEmitter emitter = notificationService.subscribe(handle);
        return emitter;
    }

    @GetMapping("")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam("direction") String direction,
            @RequestParam("timestamp") Long timestamp,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        List<NotificationResponse> notifications
                = notificationService.getNotifications(handle, direction, timestamp, size);
        if(notifications == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(notifications);
    }

}
