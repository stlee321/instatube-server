package me.stlee321.instatube.app.service;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.config.RedisConfig;
import me.stlee321.instatube.app.controller.dto.req.SetMeForm;
import me.stlee321.instatube.app.controller.dto.req.SignInForm;
import me.stlee321.instatube.app.distlock.DistLock;
import me.stlee321.instatube.app.entity.user.User;
import me.stlee321.instatube.app.entity.user.UserRepository;
import me.stlee321.instatube.app.service.dto.UserInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    public AuthService(
            UserRepository userRepository,
            ImageService imageService,
            PasswordEncoder passwordEncoder,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }
    public boolean isHandleUnique(String handle) {
        User user = userRepository.findByHandle(handle);
        // 탈퇴한지 24시간 지나기 전이면 핸들이 아직 존재한다고 봄
        String signedOut = redisTemplate.opsForValue().get("signedOut:" + handle);
        return user == null && signedOut == null;
    }

    @DistLock(keyName = "user")
    public boolean signOut(String handle, String password) {
        User user = userRepository.findByHandle(handle);
        if(user != null) {
            if(passwordEncoder.matches(password, user.getPassword())) {
                userRepository.deleteByHandle(handle);
                // 24시간동안은 같은 핸들로 가입할 수 없다.
                redisTemplate.opsForValue().set("signedOut:" + handle, handle, 24, TimeUnit.HOURS);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean validateCredential(String handle, String password) {
        User user = userRepository.findByHandle(handle);
        if(user == null) return false;
        String passwordEncoded = user.getPassword();
        return passwordEncoder.matches(password, passwordEncoded);
    }

    @DistLock(keyName = "user")
    public boolean signIn(SignInForm form) {
        if(!isHandleUnique(form.getHandle())) return false;
        // user 데이터 저장
        String passwordEncoded = passwordEncoder.encode(form.getPassword());
        User newUser = User.builder()
                .handle(form.getHandle())
                .password(passwordEncoded)
                .avatarId(form.getAvatarId())
                .build();
        try {
            userRepository.save(newUser);
            imageService.setResourceOwner(form.getAvatarId(), form.getHandle());
        }catch(Exception e) {
            return false;
        }
        return true;
    }

    @DistLock(keyName = "user")
    public boolean changePassword(String handle, String currentPwd, String newPwd) {
        User user = userRepository.findByHandle(handle);
        if(user == null) return false;
        if(!passwordEncoder.matches(currentPwd, user.getPassword())) return false;
        String newPasswordEncoded = passwordEncoder.encode(newPwd);
        user.setPassword(newPasswordEncoded);
        try {
            userRepository.save(user);
        }catch(Exception e) {
            return false;
        }
        return true;
    }

    public void setHandleLoggedOut(String handle) {
        // save handle in Logout Blacklist
        // if handle is in the blacklist
        // requests are blocked although the access token is valid.
        String key = RedisConfig.getLogoutPrefix() + handle;
        redisTemplate.opsForValue().set(key, handle, 2L, TimeUnit.HOURS);
    }

    public void setHandleLoggedIn(String handle) {
        // 레디스의 로그아웃 블랙리스트에서 삭제
        String key = RedisConfig.getLogoutPrefix() + handle;
        redisTemplate.opsForValue().getAndDelete(key);
    }

    public UserInfo getUserInfo(String handle) {
        User user = userRepository.findByHandle(handle);
        if(user == null) return null;
        UserInfo userInfo = UserInfo.builder()
                .handle(user.getHandle())
                .avatarId(user.getAvatarId())
                .build();
        return userInfo;
    }

    @DistLock(keyName = "user")
    public boolean setUserInfo(String handle, SetMeForm form) {
        User user = userRepository.findByHandle(handle);
        if(user == null) {
            return false;
        }
        user.setAvatarId(form.getAvatarId());
        try {
            userRepository.save(user);
        }catch(Exception e) {
            return false;
        }
        return true;
    }
}
