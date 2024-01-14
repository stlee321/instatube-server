package me.stlee321.instatube.app.controller.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.service.dto.UserInfo;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserInfoResponse {
    public UserInfoResponse() {}
    private String handle;
    private String avatarId;
    public static UserInfoResponse fromUserInfo(UserInfo userInfo) {
        if(userInfo == null) return null;
        return UserInfoResponse.builder()
                .handle(userInfo.getHandle())
                .avatarId(userInfo.getAvatarId())
                .build();
    }
}
