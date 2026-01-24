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
public class RedisLeaderElectionService implements LeaderElectionService, InitializingBean, DisposableBean, SmartLifecycle {

    private final StringRedisTemplate stringRedisTemplate;
    private final InstanceIdLeaseService instanceIdLeaseService;
    private final Duration leaseTtl;
    private final Duration renewEvery;

    private volatile boolean running;
    private volatile boolean isLeader = false;

    private ScheduledExecutorService scheduler;

    public RedisLeaderElectionService(
            StringRedisTemplate stringRedisTemplate,
            InstanceIdLeaseService instanceIdLeaseService,
            @Value("${app.leader.lease-ttl-seconds:30}") long leaseTtlSeconds,
            @Value("${app.leader.renew-every-seconds:10}") long renewEverySeconds
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.instanceIdLeaseService = instanceIdLeaseService;
        this.leaseTtl = Duration.ofSeconds(leaseTtlSeconds);
        this.renewEvery = Duration.ofSeconds(renewEverySeconds);
    }

    @Override
    public boolean isLeader() {
        return isLeader;
    }

    @Override
    public UUID getCurrentLeaderId() {
        String value = stringRedisTemplate.opsForValue().get("app:leader:lock");
        if (value == null) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "leader-election-renewer");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        if (running) return;
        tryAcquireLeadership();
        scheduler.scheduleAtFixedRate(this::renewLeadershipSafe,
                renewEvery.toMillis(), renewEvery.toMillis(), TimeUnit.MILLISECONDS);
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        releaseLeadership();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 1; // After instance id lease
    }

    @Override
    public void destroy() {
        stop();
        if (scheduler != null) scheduler.shutdownNow();
    }

    private void tryAcquireLeadership() {
        UUID instanceId = instanceIdLeaseService.getInstanceId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent("app:leader:lock", instanceId.toString(), leaseTtl);
        isLeader = success != null && success;
    }

    private void renewLeadershipSafe() {
        try {
            renewOrReacquireLeadership();
        } catch (Exception ignored) {
            // If Redis hiccups, next tick may succeed
        }
    }

    private void renewOrReacquireLeadership() {
        UUID instanceId = instanceIdLeaseService.getInstanceId();
        String currentValue = stringRedisTemplate.opsForValue().get("app:leader:lock");

        if (instanceId.toString().equals(currentValue)) {
            // We are the leader, renew
            stringRedisTemplate.expire("app:leader:lock", leaseTtl.toMillis(), TimeUnit.MILLISECONDS);
            isLeader = true;
        } else {
            // Try to acquire
            tryAcquireLeadership();
        }
    }

    private void releaseLeadership() {
        UUID instanceId = instanceIdLeaseService.getInstanceId();
        String currentValue = stringRedisTemplate.opsForValue().get("app:leader:lock");
        if (instanceId.toString().equals(currentValue)) {
            stringRedisTemplate.delete("app:leader:lock");
        }
        isLeader = false;
    }
}
