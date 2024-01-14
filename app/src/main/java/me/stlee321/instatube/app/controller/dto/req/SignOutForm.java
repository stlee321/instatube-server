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
public class SignOutForm {
    public SignOutForm() {}
    @Handle
    private String handle;
    @Password
    private String password;
}
