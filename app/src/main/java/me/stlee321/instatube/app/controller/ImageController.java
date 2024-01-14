package me.stlee321.instatube.app.controller;

import me.stlee321.instatube.app.controller.dto.res.ImageResponse;
import me.stlee321.instatube.app.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/img")
@Validated
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/p/{imageId}")
    public ResponseEntity<String> getPostImage(@PathVariable("imageId") String imageId) {
        String url = imageService.getPostImageUrl(imageId);
        if(url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(url);
    }
    @GetMapping("/u/{avatarId}")
    public ResponseEntity<String> getAvatarImage(@PathVariable("avatarId") String avatarId) {
        String url = imageService.getUserAvatarUrl(avatarId);
        if(url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(url);
    }

    @PostMapping("/post")
    public ResponseEntity<ImageResponse> uploadPostImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        String imageId = imageService.uploadPostImage(handle, image);
        if(imageId == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(ImageResponse.builder()
                .message("post image upload success")
                .resourceId(imageId).build());
    }
    @PostMapping("/avatar/setme")
    public ResponseEntity<ImageResponse> updateAvatarImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication
    ) {
        if(authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String handle = authentication.getName();
        String avatarId = imageService.uploadUserAvatar(handle, image);
        if(avatarId == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(ImageResponse.builder()
                .message("avatar image upload success")
                .resourceId(avatarId).build());
    }

    @PostMapping("/avatar/signin")
    public ResponseEntity<ImageResponse> createAvatarImage(
            @RequestParam("image") MultipartFile image
    ) {
        String avatarId = imageService.uploadUserAvatar("", image);
        if(avatarId == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(ImageResponse.builder()
                .message("avatar image upload success")
                .resourceId(avatarId).build());
    }
}