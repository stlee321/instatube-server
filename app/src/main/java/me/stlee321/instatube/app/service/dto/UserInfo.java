package me.stlee321.instatube.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserInfo {
    public UserInfo() {}
    private String handle;
    private String avatarId;
}
