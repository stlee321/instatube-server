package me.stlee321.instatube.app.controller.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.stlee321.instatube.app.service.dto.Notification;
import org.springframework.core.convert.converter.Converter;

public class NotificationConverter implements Converter<String, Notification> {
    private final ObjectMapper objectMapper;
    public NotificationConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public Notification convert(String source) {
        Notification notification;
        try {
            notification = objectMapper.readValue(source, Notification.class);
        }catch(Exception e) {
            notification = null;
        }
        return notification;
    }
}
