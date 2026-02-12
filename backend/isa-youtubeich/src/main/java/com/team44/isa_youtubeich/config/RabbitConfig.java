package com.team44.isa_youtubeich.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue benchmarkQueue() {
        return new Queue("benchmark_queue", false);
    }
}