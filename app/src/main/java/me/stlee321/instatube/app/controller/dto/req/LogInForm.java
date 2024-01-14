package me.stlee321.instatube.app.controller.dto.req;

import jakarta.validation.constraints.NotBlank;
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
public class LogInForm {
    public LogInForm() {}
    @Handle
    @NotBlank
    private String handle;
    @Password
    private String password;
}
