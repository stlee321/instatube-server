package me.stlee321.instatube.app.service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Notification {
    public Notification() {}
    private String from;
    private String target;
    private String type;
    private String link;
    private Long timestamp;

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        }catch(Exception e) {
            return "error";
        }
    }
}
