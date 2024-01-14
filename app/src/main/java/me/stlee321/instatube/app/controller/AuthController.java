package me.stlee321.instatube.app.controller;

import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.req.ChangePwdForm;
import me.stlee321.instatube.app.controller.dto.req.LogInForm;
import me.stlee321.instatube.app.controller.dto.req.SignInForm;
import me.stlee321.instatube.app.controller.dto.req.SignOutForm;
import me.stlee321.instatube.app.controller.dto.res.LogInResponse;
import me.stlee321.instatube.app.controller.dto.res.RefreshResponse;
import me.stlee321.instatube.app.service.AuthService;
import me.stlee321.instatube.app.service.NotificationService;
import me.stlee321.instatube.app.service.SignOutService;
import me.stlee321.instatube.app.validator.handle.Handle;
import me.stlee321.instatube.jwt.JwtTokenProvider;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final SignOutService signOutService;
    private final NotificationService notificationService;
    public AuthController(
            JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            SignOutService signOutService,
            NotificationService notificationService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.signOutService = signOutService;
        this.notificationService = notificationService;
    }
    @PostMapping(value = "/signin")
    public ResponseEntity<String> singIn(@Valid @RequestBody SignInForm form) {
        boolean handleUnique = authService.isHandleUnique(form.getHandle());
        if(!handleUnique) return ResponseEntity.badRequest().body("handle already exist");
        // 회원가입 처리
        if(!authService.signIn(form)) {
            return ResponseEntity.badRequest().body("sign in failed, try again");
        }
        return ResponseEntity.ok("sign in ok");
    }
    @PostMapping("/signout")
    public ResponseEntity<String> signOut(@RequestBody SignOutForm form, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.badRequest().body("authentication is null");
        }
        String handle = authentication.getName();
        if(!handle.equals(form.getHandle())) {
            return ResponseEntity.badRequest().body("handle not match");
        }
        if(authService.signOut(form.getHandle(), form.getPassword())) {
            signOutService.processSignOut(form.getHandle());
            return ResponseEntity.ok("signed out");
        }
        return ResponseEntity.badRequest().body("no such user");
    }

    @GetMapping("/unique/handle")
    public ResponseEntity<String> checkHandleUniqueness(@RequestParam("v") @Handle String handle) {
        if(handle == null || handle.isEmpty()) {
            return ResponseEntity.badRequest().body("empty handle");
        }
        if(authService.isHandleUnique(handle))
            return ResponseEntity.ok("true");
        return ResponseEntity.ok().body("false");
    }
    @PostMapping("/login")
    public ResponseEntity<LogInResponse> login(@RequestBody LogInForm form) {
        boolean validated = authService.validateCredential(form.getHandle(), form.getPassword());
        if(validated) {
            String handle = form.getHandle();
            authService.setHandleLoggedIn(handle);
            String accessToken = jwtTokenProvider.createAccessToken(handle);
            String refreshToken = jwtTokenProvider.createRefreshToken(handle);
            // 리프레시 토큰은 쿠키에 등록한다
            ResponseCookie cookie = ResponseCookie.from("refresh")
                    .sameSite("Strict")
                    .secure(true)
                    .path("/api/auth/refresh")
                    .httpOnly(true)
                    .value(refreshToken).build();
            // 액세스 토큰은 body에 전달
            LogInResponse res = LogInResponse.builder()
                    .message("login success")
                    .accessToken(accessToken)
                    .build();
            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body(res);
        }
        LogInResponse res = LogInResponse.builder()
                .message("invalid credential").build();
        return ResponseEntity.badRequest().body(res);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.badRequest().body("authentication is null");
        }
        String handle = authentication.getName();
        // 로그아웃 하면 레디스 블랙리스트에 등록됨
        // 로그인 하기 전까지 모든 토큰 사용이 forbidden
        authService.setHandleLoggedOut(handle);
        // 리프레시 토큰 초기화
        ResponseCookie cookie = ResponseCookie.from("refresh")
                .path("/api/auth/refresh")
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .value("").build();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body("logout success");
    }
    @PostMapping("/pwd/change")
    public ResponseEntity<String> changePassword(@RequestBody ChangePwdForm form, Authentication authentication) {
        if(authentication == null) {
            return ResponseEntity.badRequest().body("authentication is null");
        }
        String handle = authentication.getName();
        if(authService.changePassword(handle, form.getCurrentPwd(), form.getNewPwd())) {
            notificationService.publishAlertResetPassword(handle);
            return ResponseEntity.ok("password changed successfully");
        }
        return ResponseEntity.badRequest().body("wrong password... try again");
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(@CookieValue("refresh") Cookie tokenCookie) {
        if(tokenCookie == null) {
            return ResponseEntity.badRequest().body(
                    RefreshResponse.builder().message("no refresh token").build()
            );
        }
        String token = tokenCookie.getValue();
        if(token == null || token.isBlank()) return ResponseEntity.badRequest().body(
                RefreshResponse.builder().message("invalid refresh token").build()
        );
        String handle = jwtTokenProvider.getSubject(token);
        if(handle == null || handle.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(RefreshResponse.builder().message("refresh token broken").build());
        }
        String accessToken = jwtTokenProvider.createAccessToken(handle);
        String refreshToken = jwtTokenProvider.createRefreshToken(handle);
        ResponseCookie cookie = ResponseCookie.from("refresh")
                .path("/api/auth/refresh")
                .sameSite("Strict")
                .secure(true)
                .httpOnly(true)
                .value(refreshToken).build();
        RefreshResponse res = RefreshResponse.builder()
                .message("refresh success")
                .accessToken(accessToken).build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(res);
    }
}
