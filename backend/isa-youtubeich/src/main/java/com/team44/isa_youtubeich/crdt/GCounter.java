package com.team44.isa_youtubeich.crdt;

import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.atomic.AtomicLongArray;

public class GCounter implements MessageListener {

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
        String message = id + ":" + newValue;
        redisTemplate.convertAndSend(channel, message);
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
        String msg = new String(message.getBody());
        String[] parts = msg.split(":");
        int id = Integer.parseInt(parts[0]);
        long value = Long.parseLong(parts[1]);
        counters.updateAndGet(id, current -> Math.max(current, value));
    }
}
