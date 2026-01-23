package com.team44.isa_youtubeich.config;

import com.team44.isa_youtubeich.crdt.GCounter;
import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public GCounter gCounter(StringRedisTemplate redisTemplate, InstanceIdLeaseService leaseService, RedisMessageListenerContainer container, @Value("${app.instance-id.max-instances:16}") int maxInstances, @Value("${gcounter.channel:gcounter:updates}") String channel) {
        return new GCounter(redisTemplate, leaseService, container, maxInstances, channel);
    }
}
