package com.purva.grid07.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisGuardrailService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_BOT_REPLIES = 100;
    private static final int MAX_DEPTH_LEVEL = 20;
    private static final long COOLDOWN_MINUTES = 10;

    //  Virality Score

    public void incrementViralityScore(Long postId, String interactionType) {
        String key = "post:" + postId + ":virality_score";
        switch (interactionType) {
            case "BOT_REPLY"      -> redisTemplate.opsForValue().increment(key, 1);
            case "HUMAN_LIKE"     -> redisTemplate.opsForValue().increment(key, 20);
            case "HUMAN_COMMENT"  -> redisTemplate.opsForValue().increment(key, 50);
        }
    }

    public String getViralityScore(Long postId) {
        String key = "post:" + postId + ":virality_score";
        String score = redisTemplate.opsForValue().get(key);
        return score != null ? score : "0";
    }

    //  Horizontal Cap (max 100 bot replies per post)

    public boolean checkAndIncrementBotCount(Long postId) {
        String key = "post:" + postId + ":bot_count";
        Long count = redisTemplate.opsForValue().increment(key);
        if (count > MAX_BOT_REPLIES) {
            // Roll back the increment
            redisTemplate.opsForValue().decrement(key);
            return false; // BLOCKED
        }
        return true; // ALLOWED
    }

    // Vertical Cap (max depth 20)

    public boolean checkDepthLevel(int depthLevel) {
        return depthLevel <= MAX_DEPTH_LEVEL;
    }

    // Cooldown Cap (bot can't interact with same human within 10 mins)

    public boolean checkAndSetCooldown(Long botId, Long userId) {
        String key = "cooldown:bot_" + botId + ":human_" + userId;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false; // BLOCKED - cooldown active
        }
        // Set cooldown key with 10 minute TTL
        redisTemplate.opsForValue().set(key, "1", COOLDOWN_MINUTES, TimeUnit.MINUTES);
        return true;
    }
}