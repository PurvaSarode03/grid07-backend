package com.purva.grid07.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StringRedisTemplate redisTemplate;

    private static final long NOTIFICATION_COOLDOWN_MINUTES = 15;

    public void handleBotNotification(Long userId, String botName) {
        String cooldownKey = "notif_cooldown:user_" + userId;
        String pendingKey  = "user:" + userId + ":pending_notifs";

        Boolean onCooldown = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(onCooldown)) {
            // Queue the notification
            redisTemplate.opsForList().rightPush(pendingKey, botName + " replied to your post");
            System.out.println("Notification queued for user " + userId);
        } else {
            // Send immediately and set cooldown
            System.out.println("Push Notification Sent to User " + userId + ": " + botName + " replied to your post");
            redisTemplate.opsForValue().set(cooldownKey, "1", NOTIFICATION_COOLDOWN_MINUTES, TimeUnit.MINUTES);
        }
    }
}