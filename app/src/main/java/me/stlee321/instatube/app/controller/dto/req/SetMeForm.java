package me.stlee321.instatube.app.controller.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SetMeForm {
    public SetMeForm() {}
    private String avatarId;
}
