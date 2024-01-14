package me.stlee321.instatube.app.controller.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.stlee321.instatube.app.validator.handle.Handle;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReplyForm {
    public ReplyForm() {}
    @Handle
    private String handle;
    @Length(max = 1500)
    @NotBlank
    private String content;
    @NotBlank
    private String postId;
    private String targetId;
}
