package com.purva.grid07.controller;

import com.purva.grid07.entity.Comment;
import com.purva.grid07.entity.Post;
import com.purva.grid07.service.PostService;
import com.purva.grid07.service.RedisGuardrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final RedisGuardrailService guardrailService;

    // Create Post
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post saved = postService.createPost(post);
        return ResponseEntity.ok(saved);
    }

    // Like Post
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId) {
        postService.likePost(postId);
        return ResponseEntity.ok("Post liked! Virality score updated.");
    }

    // Add Comment
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestBody Comment comment,
            @RequestParam(defaultValue = "false") boolean isBot,
            @RequestParam(defaultValue = "0") Long botId,
            @RequestParam(defaultValue = "0") Long postAuthorId) {

        Comment saved = postService.addComment(postId, comment, isBot, botId, postAuthorId);
        return ResponseEntity.ok(saved);
    }

    // Get Virality Score
    @GetMapping("/{postId}/virality")
    public ResponseEntity<String> getViralityScore(@PathVariable Long postId) {
        String score = guardrailService.getViralityScore(postId);
        return ResponseEntity.ok("Virality Score for post " + postId + ": " + score);
    }
}