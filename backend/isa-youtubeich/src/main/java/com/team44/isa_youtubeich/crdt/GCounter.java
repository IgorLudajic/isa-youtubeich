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
import java.util.concurrent.atomic.AtomicLongArray;

public class GCounter implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(GCounter.class);

    private final StringRedisTemplate redisTemplate;
    private final InstanceIdLeaseService leaseService;
    private final RedisMessageListenerContainer container;
    private final int maxInstances;
    private final String channel;

    private AtomicLongArray counters;

    public GCounter(StringRedisTemplate redisTemplate, InstanceIdLeaseService leaseService, RedisMessageListenerContainer container, @Value("${app.instance-id.max-instances:16}") int maxInstances, @Value("${gcounter.channel:gcounter:updates}") String channel) {
        this.redisTemplate = redisTemplate;
        this.leaseService = leaseService;
        this.container = container;
        this.maxInstances = maxInstances;
        this.channel = channel;
    }

    @PostConstruct
    public void init() {
        counters = new AtomicLongArray(maxInstances);
        container.addMessageListener(this, new ChannelTopic(channel));
    }

    public void increment() {
        int id = leaseService.getInstanceId(); // 0-based index
        long newValue = counters.incrementAndGet(id);
        byte[] message;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(id);
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
        long sum = 0;
        for (int i = 0; i < maxInstances; i++) {
            sum += counters.get(i);
        }
        return sum;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(message.getBody()));
             DataInputStream dis = new DataInputStream(bais)) {
            int id = dis.readInt();
            long value = dis.readLong();
            counters.updateAndGet(id, current -> Math.max(current, value));
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}
