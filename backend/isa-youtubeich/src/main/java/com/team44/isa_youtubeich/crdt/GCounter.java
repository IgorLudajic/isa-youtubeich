package com.team44.isa_youtubeich.crdt;

import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.*;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GCounter implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(GCounter.class);

    private final StringRedisTemplate redisTemplate;
    private final InstanceIdLeaseService leaseService;
    private final RedisMessageListenerContainer container;
    private final String channel;

    private ConcurrentHashMap<UUID, AtomicLong> counters;

    public GCounter(StringRedisTemplate redisTemplate, InstanceIdLeaseService leaseService, RedisMessageListenerContainer container, @Value("${gcounter.channel:gcounter:updates}") String channel) {
        this.redisTemplate = redisTemplate;
        this.leaseService = leaseService;
        this.container = container;
        this.channel = channel;
    }

    @PostConstruct
    public void init() {
        counters = new ConcurrentHashMap<>();
        container.addMessageListener(this, new ChannelTopic(channel));
    }

    public void increment() {
        UUID id = leaseService.getInstanceId(); // 0-based index
        AtomicLong counter = counters.computeIfAbsent(id, k -> new AtomicLong());
        long newValue = counter.incrementAndGet();
        byte[] message;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            writeUUID(dos, id);
            dos.writeLong(newValue);
            message = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }

        try {
            String payload = Base64.getEncoder().encodeToString(message);
            redisTemplate.convertAndSend(channel, payload);
        } catch (Exception e) {
            log.warn("Failed to publish counter update to Redis; continuing with local state", e);
        }
    }

    public long getValue() {
        return counters.values().stream().mapToLong(AtomicLong::get).sum();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(message.getBody()));
             DataInputStream dis = new DataInputStream(bais)) {
            UUID id = readUUID(dis);
            long value = dis.readLong();
            counters.compute(id, (k, v) -> {
                if (v == null) {
                    return new AtomicLong(value);
                } else {
                    v.set(Math.max(v.get(), value));
                    return v;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }

    protected void writeUUID(DataOutputStream dos, UUID uuid) throws IOException {
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    protected UUID readUUID(DataInputStream dis) throws IOException {
        long mostSigBits = dis.readLong();
        long leastSigBits = dis.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    protected ConcurrentHashMap<UUID, AtomicLong> getCounters() {
        return counters;
    }

    protected StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    protected String getChannel() {
        return channel;
    }

    protected InstanceIdLeaseService getLeaseService() {
        return leaseService;
    }
}
