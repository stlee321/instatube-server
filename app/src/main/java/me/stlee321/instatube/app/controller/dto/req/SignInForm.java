package me.stlee321.instatube.app.controller.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.validator.handle.Handle;
import me.stlee321.instatube.app.validator.password.Password;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SignInForm {
    public SignInForm() {}
    @Password
    private String password;
    @Handle
    private String handle;
    private String avatarId;
}
