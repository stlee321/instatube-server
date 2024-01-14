package me.stlee321.instatube.app.controller.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ImageResponse {
    public ImageResponse() {}
    private String message;
    private String resourceId;
}
