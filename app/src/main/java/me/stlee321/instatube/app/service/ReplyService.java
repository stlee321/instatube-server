package me.stlee321.instatube.app.service;

import lombok.extern.slf4j.Slf4j;
import me.stlee321.instatube.app.controller.dto.req.ReplyForm;
import me.stlee321.instatube.app.controller.dto.req.ReplyUpdateForm;
import me.stlee321.instatube.app.distlock.DistLock;
import me.stlee321.instatube.app.entity.reply.Reply;
import me.stlee321.instatube.app.entity.reply.ReplyRepository;
import me.stlee321.instatube.app.service.dto.ReplyDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReplyService {

    private final ReplyRepository replyRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REPLY_REPLY_PREFIX = "reply:reply:count:";
    private static final String POST_REPLY_PREFIX = "post:reply:count:";

    public ReplyService(ReplyRepository replyRepository, RedisTemplate<String, String> redisTemplate) {
        this.replyRepository = replyRepository;
        this.redisTemplate = redisTemplate;
    }
    public List<ReplyDetail> getRepliesAfter(Long timestamp, String postId, String targetId, int size) {
        List<ReplyDetail> replies;
        if(targetId == null) {
            replies = replyRepository.getRepliesAfter(timestamp, postId, Pageable.ofSize(size))
                    .stream().map(ReplyDetail::fromReply).toList();
            return replies;
        }
        replies = replyRepository.getRepliesAfterForTarget(timestamp, postId, targetId, Pageable.ofSize(size))
                .stream().map(ReplyDetail::fromReply).toList();
        return replies;
    }

    @DistLock(keyName = "reply")
    public ReplyDetail createReply(String handle, ReplyForm form) {
        Reply reply = Reply.builder()
                .handle(handle)
                .postId(form.getPostId())
                .content(form.getContent())
                .replyId(generateReplyId())
                .targetId(form.getTargetId())
                .build();
        try {
            reply = replyRepository.save(reply);
            increasePostReplyCount(reply.getPostId());
            if(reply.getTargetId() != null) {
                increaseReplyReplyCount(reply.getTargetId());
            }
        }catch(Exception e) {
            return null;
        }
        return ReplyDetail.fromReply(reply);
    }

    @DistLock(keyName = "reply")
    public ReplyDetail updateReply(String handle, String replyId, ReplyUpdateForm form) {
        Reply reply = replyRepository.findByReplyId(replyId);
        reply.setContent(form.getContent());
        try {
            replyRepository.save(reply);
        }catch(Exception e) {
            return null;
        }
        return ReplyDetail.fromReply(reply);
    }

    @DistLock(keyName = "reply")
    public void deleteReply(String handle, String replyId) {
        Reply reply = replyRepository.findByReplyId(replyId);
        if(reply != null) {
            replyRepository.delete(reply);
            decreasePostReplyCount(reply.getPostId());
            if(reply.getTargetId() == null || reply.getTargetId().isEmpty()) {
                replyRepository.deleteRepliesTargeting(reply.getReplyId());
                deletePostReplyCount(reply.getPostId());
                deleteReplyReplyCount(reply.getReplyId());
            }else{
                decreaseReplyReplyCount(reply.getTargetId());
            }
        }
    }

    private void increaseReplyReplyCount(String replyId) {
        String key = REPLY_REPLY_PREFIX + replyId;
        redisTemplate.opsForValue().increment(key);
    }
    private void decreaseReplyReplyCount(String replyId) {
        String key = REPLY_REPLY_PREFIX + replyId;
        redisTemplate.opsForValue().decrement(key);
    }
    private void increasePostReplyCount(String postId) {
        String key = POST_REPLY_PREFIX + postId;
        redisTemplate.opsForValue().increment(key);
    }
    private void decreasePostReplyCount(String postId) {
        String key = POST_REPLY_PREFIX + postId;
        redisTemplate.opsForValue().decrement(key);
    }
    private void deletePostReplyCount(String postId) {
        String key = POST_REPLY_PREFIX + postId;
        redisTemplate.opsForValue().getAndDelete(key);
    }
    private void deleteReplyReplyCount(String replyId) {
        String key = REPLY_REPLY_PREFIX + replyId;
        redisTemplate.opsForValue().getAndDelete(key);
    }

    public int getReplyCountOfReply(String replyId) {
        String key = REPLY_REPLY_PREFIX + replyId;
        String count = redisTemplate.opsForValue().get(key);
        if(count == null) {
            count = replyRepository.getReplyCountOfReply(replyId);
            redisTemplate.opsForValue().set(key, count);
        };
        return Integer.parseInt(count);
    }
    public int getReplyCountOfPost(String postId) {
        String key = POST_REPLY_PREFIX + postId;
        String count = redisTemplate.opsForValue().get(key);
        if(count == null) {
            count = replyRepository.getReplyCountOfPost(postId);
            redisTemplate.opsForValue().set(key, count);
        }
        return Integer.parseInt(count);
    }

    public boolean replyExists(String replyId) {
        Reply reply = replyRepository.findByReplyId(replyId);
        return reply != null;
    }

    public boolean isReplyOwnedBy(String handle, String replyId) {
        Reply reply = replyRepository.findByReplyId(replyId);
        if(reply == null) return false;
        return reply.getHandle().equals(handle);
    }

    String generateReplyId() {
        return UUID.randomUUID().toString();
    }

    @DistLock(keyName = "reply")
    public void deleteAllReplyOf(String handle) {
        var replies = replyRepository.findByHandle(handle);
        replies.forEach(r -> {
            replyRepository.deleteRepliesTargeting(r.getReplyId());
        });
        replyRepository.deleteByHandle(handle);
    }

    public ReplyDetail getReply(String replyId) {
        Reply reply = replyRepository.findByReplyId(replyId);
        if(reply == null) return null;
        return ReplyDetail.fromReply(reply);
    }
}
