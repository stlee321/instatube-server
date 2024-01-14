package me.stlee321.instatube.app.controller.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReplyUpdateForm {
    public ReplyUpdateForm() {}
    @NotBlank
    private String replyId;
    @NotBlank
    private String content;
}
