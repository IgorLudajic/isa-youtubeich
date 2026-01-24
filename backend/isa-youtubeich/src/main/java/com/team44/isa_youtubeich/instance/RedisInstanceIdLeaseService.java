package com.team44.isa_youtubeich.instance;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RedisInstanceIdLeaseService implements InstanceIdLeaseService, InitializingBean, DisposableBean, SmartLifecycle {

    private final StringRedisTemplate stringRedisTemplate;
    private final Duration leaseTtl;
    private final Duration renewEvery;

    private final String ownerToken = UUID.randomUUID().toString();
    private volatile UUID instanceId;
    private volatile boolean running;

    private ScheduledExecutorService scheduler;

    public RedisInstanceIdLeaseService(
            StringRedisTemplate stringRedisTemplate,
            @Value("${app.instance-id.lease-ttl-seconds:30}") long leaseTtlSeconds,
            @Value("${app.instance-id.renew-every-seconds:10}") long renewEverySeconds
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.leaseTtl = Duration.ofSeconds(leaseTtlSeconds);
        this.renewEvery = Duration.ofSeconds(renewEverySeconds);
    }

    public UUID getInstanceId() {
        UUID id = instanceId;
        if (id == null) throw new IllegalStateException("Instance id not allocated yet");
        return id;
    }

    @Override
    public void afterPropertiesSet() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "instance-id-lease-renewer");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        if (running) return;
        allocateOrThrow();
        scheduler.scheduleAtFixedRate(this::renewLeaseSafe,
                renewEvery.toMillis(), renewEvery.toMillis(), TimeUnit.MILLISECONDS);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        try {
            releaseLease();
        } catch (Exception e) {
            // Ignore exceptions during shutdown
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void destroy() {
        stop();
        if (scheduler != null) scheduler.shutdownNow();
    }

    private void allocateOrThrow() {
        UUID id = UUID.randomUUID();
        String key = "app:instance:lease:" + id.toString();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, ownerToken, leaseTtl);

        if (success == null || !success) {
            throw new IllegalStateException("Failed to allocate unique instance id");
        }
        this.instanceId = id;
    }

    private void renewLeaseSafe() {
        try {
            renewLeaseOrReacquire();
        } catch (Exception ignored) {
            // If Redis hiccups temporarily, next tick may succeed.
            // If lease is lost, reacquire will happen below.
        }
    }

    private void renewLeaseOrReacquire() {
        UUID id = instanceId;
        if (id == null) return;

        String key = "app:instance:lease:" + id;
        Long renewed = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

        if (renewed == null || renewed <= 0) {
            // lease lost (expired or stolen): attempt to reacquire a new id
            this.instanceId = null;
            allocateOrThrow();
        } else {
            stringRedisTemplate.expire(key, leaseTtl.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void releaseLease() {
        UUID id = instanceId;
        if (id == null) return;

        String key = "app:instance:lease:" + id;
        stringRedisTemplate.delete(key);

        instanceId = null;
    }
}
