package com.purva.grid07.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationSweeper {

    private final StringRedisTemplate redisTemplate;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void sweepPendingNotifications() {
        System.out.println("⏰ Sweeper running — checking pending notifications...");

        // Find all pending notification keys
        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) {
            System.out.println("No pending notifications found.");
            return;
        }

        for (String key : keys) {
            // Extract userId from key format "user:{id}:pending_notifs"
            String userId = key.split(":")[1];

            // Pop all messages from the list
            List<String> notifications = redisTemplate.opsForList().range(key, 0, -1);

            if (notifications != null && !notifications.isEmpty()) {
                String firstName = notifications.get(0);
                int othersCount  = notifications.size() - 1;

                if (othersCount > 0) {
                    System.out.println("Summarized Push Notification to User " + userId
                            + ": " + firstName + " and [" + othersCount + "] others interacted with your posts.");
                } else {
                    System.out.println("Summarized Push Notification to User " + userId
                            + ": " + firstName);
                }

                // Clear the Redis list
                redisTemplate.delete(key);
            }
        }
    }
}