package com.team44.isa_youtubeich.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class RedisConfig {

    public static final String PREMIERE_SCHEDULE_TOPIC = "premiere-schedule";

    @Bean
    public MessageListenerAdapter premiereMessageListenerAdapter(PremiereMessageListener premiereMessageListener) {
        return new MessageListenerAdapter(premiereMessageListener);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter premiereMessageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(premiereMessageListenerAdapter, new ChannelTopic(PREMIERE_SCHEDULE_TOPIC));
        return container;
    }

    @Bean(name = "premiereTaskScheduler")
    @Primary
    public ThreadPoolTaskScheduler premiereTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(16);
        scheduler.setThreadNamePrefix("premiere-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
