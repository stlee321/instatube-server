package me.stlee321.instatube.app.service;

import me.stlee321.instatube.app.graph.repository.UserNodeRepository;
import org.springframework.stereotype.Service;

@Service
public class SignOutService {
    private final PostService postService;
    private final ReplyService replyService;
    private final UserNodeRepository userNodeRepository;
    private final ImageService imageService;
    private final FollowService followService;
    public SignOutService(
            PostService postService,
            ReplyService replyService,
            UserNodeRepository userNodeRepository,
            ImageService imageService,
            FollowService followService
    ) {
        this.postService = postService;
        this.replyService = replyService;
        this.userNodeRepository = userNodeRepository;
        this.imageService = imageService;
        this.followService = followService;
    }
    public void processSignOut(String handle) {
        postService.deleteAllPostsOf(handle);
        replyService.deleteAllReplyOf(handle);
        userNodeRepository.deleteUser(handle);
        imageService.detachOwnership(handle);
        followService.clearAllCache(handle);
    }
}
