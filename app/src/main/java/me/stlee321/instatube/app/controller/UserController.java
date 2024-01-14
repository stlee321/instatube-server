package me.stlee321.instatube.app.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.req.SetMeForm;
import me.stlee321.instatube.app.controller.dto.res.UserInfoResponse;
import me.stlee321.instatube.app.service.AuthService;
import me.stlee321.instatube.app.service.dto.UserInfo;
import me.stlee321.instatube.app.validator.handle.Handle;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Validated
@Slf4j
public class UserController {
    private final AuthService authService;
    public UserController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMe(Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.badRequest().body(null);
        }
        String handle = authentication.getName();
        UserInfo userInfo = authService.getUserInfo(handle);
        return ResponseEntity.ok().body(UserInfoResponse.fromUserInfo(userInfo));
    }
    @GetMapping("/{handle}")
    public ResponseEntity<UserInfoResponse> getUser(@Handle @PathVariable("handle") String handle) {
        UserInfo userInfo = authService.getUserInfo(handle);
        if(userInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(UserInfoResponse.fromUserInfo(userInfo));
    }

    @PostMapping("/me")
    public ResponseEntity<UserInfoResponse> setMe(
            @Valid @RequestBody SetMeForm form,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        boolean result = authService.setUserInfo(handle, form);
        if(result) {
            UserInfo userInfo = authService.getUserInfo(handle);
            return ResponseEntity.ok().body(UserInfoResponse.fromUserInfo(userInfo));
        }
        return ResponseEntity.badRequest().build();
    }
}
