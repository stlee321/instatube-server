package me.stlee321.instatube.app.controller.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.validator.password.Password;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChangePwdForm {
    public ChangePwdForm() {}
    @Password
    private String currentPwd;
    @Password
    private String newPwd;
}
