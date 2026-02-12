package com.team44.isa_youtubeich.instance;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Distributed, per-video lease to ensure only one instance transcodes a given video at a time.
 *
 * Implementation: Redis SET key value NX PX ttl.
 * - key: app:transcode:lock:{videoId}
 * - value: instance UUID
 */
@Component
public class RedisVideoTranscodeLeaseService {

    private final StringRedisTemplate stringRedisTemplate;
    private final InstanceIdLeaseService instanceIdLeaseService;

    @Getter
    private final Duration leaseTtl;

    public RedisVideoTranscodeLeaseService(
            StringRedisTemplate stringRedisTemplate,
            InstanceIdLeaseService instanceIdLeaseService,
            @Value("${app.transcode.lease-ttl-ms:15000}") long leaseTtlMs
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.instanceIdLeaseService = instanceIdLeaseService;
        this.leaseTtl = Duration.ofMillis(leaseTtlMs);
    }

    public boolean tryAcquire(long videoId) {
        UUID instanceId = instanceIdLeaseService.getInstanceId();
        String key = key(videoId);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, instanceId.toString(), leaseTtl);
        return ok != null && ok;
    }

    public boolean isHeldByMe(long videoId) {
        UUID instanceId = instanceIdLeaseService.getInstanceId();
        String current = stringRedisTemplate.opsForValue().get(key(videoId));
        return instanceId.toString().equals(current);
    }

    /**
     * Best-effort renew. Returns true if renewed (we owned it), false otherwise.
     */
    public boolean renewIfHeld(long videoId) {
        if (!isHeldByMe(videoId)) return false;
        stringRedisTemplate.expire(key(videoId), leaseTtl);
        return true;
    }

    /**
     * Release only if we currently own the lease.
     */
    public void releaseIfHeld(long videoId) {
        if (!isHeldByMe(videoId)) return;
        stringRedisTemplate.delete(key(videoId));
    }

    public boolean isLeaseActive(long videoId) {
        return stringRedisTemplate.hasKey(key(videoId)) != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(key(videoId)));
    }

    private String key(long videoId) {
        return "app:transcode:lock:" + videoId;
    }
}
