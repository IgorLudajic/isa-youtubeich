package com.team44.isa_youtubeich.util;

import com.team44.isa_youtubeich.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class CommentsRateLimiter {

    private final Map<String, Queue<Long>> userLogs = new ConcurrentHashMap<>();

    @Value("${app.comment-rate-limit.count:60}")
    private int MAX_COMMENTS;

    @Value("${app.comment-rate-limit.window:3600000}")
    private long WINDOW_MS;

    public void checkLimit(String username){
        long now = System.currentTimeMillis();

        Queue<Long> timestamps = userLogs.computeIfAbsent(username, k -> new ConcurrentLinkedQueue<>());

        timestamps.removeIf(timestamp -> now - timestamp > WINDOW_MS);

        if (timestamps.size() >= MAX_COMMENTS){
            throw new RateLimitExceededException("Rate limit exceeded: 60 comments per hour.");
        }

        timestamps.add(now);
    }

    public void reset(){
        userLogs.clear();
    }
}
