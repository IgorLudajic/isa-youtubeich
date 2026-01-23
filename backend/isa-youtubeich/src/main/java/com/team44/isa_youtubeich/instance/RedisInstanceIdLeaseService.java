package com.team44.isa_youtubeich.instance;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RedisInstanceIdLeaseService implements InstanceIdLeaseService, InitializingBean, DisposableBean, SmartLifecycle {

    private final StringRedisTemplate stringRedisTemplate;
    private final int maxInstances;
    private final Duration leaseTtl;
    private final Duration renewEvery;

    private final RedisScript<Long> allocateScript = RedisScript.of("""
            for i=0, tonumber(ARGV[1])-1 do
              local key = KEYS[1] .. i
              if redis.call('SET', key, ARGV[2], 'NX', 'PX', ARGV[3]) then
                return i
              end
            end
            return -1
            """, Long.class);

    private final RedisScript<Long> renewScript = RedisScript.of("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
              return redis.call('PEXPIRE', KEYS[1], ARGV[2])
            else
              return 0
            end
            """, Long.class);

    private final RedisScript<Long> releaseScript = RedisScript.of("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
              return redis.call('DEL', KEYS[1])
            else
              return 0
            end
            """, Long.class);

    private final String ownerToken = UUID.randomUUID().toString();
    private volatile Integer instanceId;
    private volatile boolean running;

    private ScheduledExecutorService scheduler;

    public RedisInstanceIdLeaseService(
            StringRedisTemplate stringRedisTemplate,
            @Value("${app.instance-id.max-instances:16}") int maxInstances,
            @Value("${app.instance-id.lease-ttl-seconds:30}") long leaseTtlSeconds,
            @Value("${app.instance-id.renew-every-seconds:10}") long renewEverySeconds
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.maxInstances = maxInstances;
        this.leaseTtl = Duration.ofSeconds(leaseTtlSeconds);
        this.renewEvery = Duration.ofSeconds(renewEverySeconds);
    }

    public int getInstanceId() {
        Integer id = instanceId;
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
        releaseLease();
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
        List<String> keys = List.of("app:instance:lease:");
        Object[] args = {Integer.toString(maxInstances), ownerToken, Long.toString(leaseTtl.toMillis())};
        Long id = stringRedisTemplate.execute(allocateScript, keys, args);

        if (id == null || id == -1L) {
            throw new IllegalStateException("No instance id available in pool 0.." + (maxInstances - 1));
        }
        this.instanceId = id.intValue();
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
        Integer id = instanceId;
        if (id == null) return;

        String key = "app:instance:lease:" + id;
        List<String> keys = List.of(key);
        Object[] args = {ownerToken, Long.toString(leaseTtl.toMillis())};
        Long renewed = stringRedisTemplate.execute(renewScript, keys, args);

        if (renewed == null || renewed == 0L) {
            // lease lost (expired or stolen): attempt to reacquire a new id
            this.instanceId = null;
            allocateOrThrow();
        }
    }

    private void releaseLease() {
        Integer id = instanceId;
        if (id == null) return;

        String key = "app:instance:lease:" + id;
        List<String> keys = List.of(key);
        Object[] args = {ownerToken};
        stringRedisTemplate.execute(releaseScript, keys, args);

        instanceId = null;
    }
}
