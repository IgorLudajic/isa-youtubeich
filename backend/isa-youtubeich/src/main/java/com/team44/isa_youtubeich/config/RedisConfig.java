package com.team44.isa_youtubeich.config;

import com.team44.isa_youtubeich.crdt.GCounter;
import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public GCounter gCounter(StringRedisTemplate redisTemplate, InstanceIdLeaseService leaseService, RedisMessageListenerContainer container, @Value("${gcounter.channel:gcounter:updates}") String channel) {
        return new GCounter(redisTemplate, leaseService, container, channel);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory, @Value("${shedlock.environment:default}") String env) {
        return new RedisLockProvider(connectionFactory, env);
    }
}
