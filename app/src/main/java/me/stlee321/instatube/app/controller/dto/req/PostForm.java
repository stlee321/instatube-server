package me.stlee321.instatube.app.controller.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostForm {
    public PostForm() {}
    @Length(max = 100)
    @NotBlank
    private String title;
    @Length(max = 2000)
    private String content;
    private String imageId;
}
