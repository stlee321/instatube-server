package me.stlee321.instatube.app.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import me.stlee321.instatube.app.distlock.DistLock;
import me.stlee321.instatube.app.entity.ownership.OwnershipRepository;
import me.stlee321.instatube.app.entity.ownership.ResourceOwnership;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ImageService {

    private final S3Template s3Template;
    private final OwnershipRepository ownershipRepository;
    private static final String BUCKET_NAME = "instatube";
    private static final String POST_PREFIX = "post/";
    private static final String AVATAR_PREFIX = "avatar/";

    public ImageService(
            S3Template s3Template,
            OwnershipRepository ownershipRepository
    ) {
        this.s3Template = s3Template;
        this.ownershipRepository = ownershipRepository;
    }
    public String getPostImageUrl(String imageId) {
        String url = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + POST_PREFIX + imageId;
        return url;
    }

    public String uploadPostImage(String handle, MultipartFile image) {
        String imageId = generateUUID();
        String objectKey = POST_PREFIX + imageId;
        try {
            ObjectMetadata metadata = ObjectMetadata.builder()
                            .contentType(image.getContentType()).build();
            s3Template.upload(BUCKET_NAME, objectKey, image.getInputStream(), metadata);
            saveOwnership(handle, imageId);
            return imageId;
        }catch(Exception e) {
            return null;
        }
    }

    public boolean deletePostImage(String handle, String imageId) {
        try {
            String objectKey = POST_PREFIX + imageId;
            s3Template.deleteObject(BUCKET_NAME, objectKey);
            deleteOwnership(handle, imageId);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public String getUserAvatarUrl(String avatarId) {
        String url = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + AVATAR_PREFIX + avatarId;
        return url;
    }

    public String uploadUserAvatar(String handle, MultipartFile avatar) {
        String avatarId = generateUUID();
        String objectKey = AVATAR_PREFIX + avatarId;
        try {
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(avatar.getContentType())
                    .build();
            s3Template.upload(BUCKET_NAME, objectKey, avatar.getInputStream(), metadata);
            saveOwnership(handle, avatarId);
            return avatarId;
        }catch(Exception e) {
            return null;
        }
    }

    public boolean deleteUserAvatar(String handle, String avatarId) {
        try {
            String objectKey = AVATAR_PREFIX + avatarId;
            s3Template.deleteObject(BUCKET_NAME, objectKey);
            deleteOwnership(handle, avatarId);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    @DistLock(keyName = "image")
    private void saveOwnership(String handle, String resourceId) {
        ResourceOwnership ownership = ResourceOwnership.builder()
                .handle(handle)
                .resourceId(resourceId)
                .build();
        try {
            ownershipRepository.save(ownership);
        }catch(Exception e) {
        }
    }

    @DistLock(keyName = "image")
    private void deleteOwnership(String handle, String resourceId) {
        ResourceOwnership ownership = ownershipRepository.findByResourceId(resourceId);
        if(ownership == null) {
            return;
        }
        try {
            if(!ownership.getHandle().equals(handle)) return;
            ownershipRepository.delete(ownership);
        }catch(Exception e) {
        }
    }

    @DistLock(keyName = "image")
    public boolean isOwner(String handle, String imageId) {
        ResourceOwnership ownership = ownershipRepository.findByResourceId(imageId);
        if(ownership == null) return false;
        return ownership.getHandle().equals(handle);
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    @DistLock(keyName = "image")
    public void setResourceOwner(String resourceId, String owner) {
        ResourceOwnership ownership = ownershipRepository.findByResourceId(resourceId);
        if(ownership == null) return;
        ownership.setHandle(owner);
        ownershipRepository.save(ownership);
    }

    @DistLock(keyName = "image")
    public void detachOwnership(String handle) {
        var resources = ownershipRepository.findByHandle(handle);
        resources.forEach(r -> {
            setResourceOwner(r.getResourceId(), "");
        });
    }
}
