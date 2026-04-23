package com.purva.grid07.service;

import com.purva.grid07.entity.Comment;
import com.purva.grid07.entity.Post;
import com.purva.grid07.repository.CommentRepository;
import com.purva.grid07.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RedisGuardrailService guardrailService;
    private final NotificationService notificationService;

    // Create Post

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    // Like Post

    public void likePost(Long postId) {
        guardrailService.incrementViralityScore(postId, "HUMAN_LIKE");
    }

    // Add Comment

    public Comment addComment(Long postId, Comment comment, boolean isBot,
                              Long botId, Long postAuthorId) {

        // 1. Vertical Cap Check
        if (!guardrailService.checkDepthLevel(comment.getDepthLevel())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Max comment depth of 20 reached");
        }

        if (isBot) {
            // 2. Horizontal Cap Check
            if (!guardrailService.checkAndIncrementBotCount(postId)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot reply limit of 100 reached for this post");
            }

            // 3. Cooldown Cap Check
            if (!guardrailService.checkAndSetCooldown(botId, postAuthorId)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot is on cooldown for this user");
            }

            // Update virality score
            guardrailService.incrementViralityScore(postId, "BOT_REPLY");

            // Handle notification
            notificationService.handleBotNotification(postAuthorId, "Bot#" + botId);

        } else {
            // Human comment — update virality
            guardrailService.incrementViralityScore(postId, "HUMAN_COMMENT");
        }

        comment.setPostId(postId);
        return commentRepository.save(comment);
    }
}