package com.team44.isa_youtubeich.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Tracks active users in the last 24 hours using Redis Sorted Set.
 * Uses user activity timestamps as scores for efficient range queries.
 * Registers a Prometheus gauge metric for monitoring.
 */
@Component
public class ActiveUsersMetricsConfig {

    private static final String ACTIVE_USERS_KEY = "metrics:active_users:24h";

    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;
    private final long ttlSeconds;

    public ActiveUsersMetricsConfig(
            StringRedisTemplate redisTemplate,
            MeterRegistry meterRegistry,
            @Value("${app.active-users.ttl-seconds:86400}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        this.ttlSeconds = ttlSeconds;
    }

    @PostConstruct
    public void init() {
        // Register gauge metric for active users count
        Gauge.builder("active_users_24h", this::getActiveUsersCount)
                .description("Number of unique active users in the last 24 hours")
                .register(meterRegistry);
    }

    /**
     * Records user activity by adding/updating their timestamp in Redis sorted set.
     * Called on each authenticated request.
     *
     * @param username the authenticated user's username
     */
    public void recordUserActivity(String username) {
        long currentTimestamp = Instant.now().getEpochSecond();
        redisTemplate.opsForZSet().add(ACTIVE_USERS_KEY, username, currentTimestamp);

        // Clean up old entries (older than 24 hours)
        long cutoffTimestamp = currentTimestamp - ttlSeconds;
        redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_USERS_KEY, 0, cutoffTimestamp);
    }

    /**
     * Gets the count of unique active users in the last 24 hours.
     *
     * @return number of active users
     */
    public double getActiveUsersCount() {
        try {
            long currentTimestamp = Instant.now().getEpochSecond();
            long cutoffTimestamp = currentTimestamp - ttlSeconds;

            // Remove stale entries first
            redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_USERS_KEY, 0, cutoffTimestamp);

            // Count remaining entries
            Long count = redisTemplate.opsForZSet().zCard(ACTIVE_USERS_KEY);
            return count != null ? count.doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
