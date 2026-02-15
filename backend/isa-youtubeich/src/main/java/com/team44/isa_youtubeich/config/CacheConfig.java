package com.team44.isa_youtubeich.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration jsonCacheConfiguration() {

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();

        JsonMapper mapper = JsonMapper.builder().activateDefaultTypingAsProperty(
                ptv, DefaultTyping.NON_FINAL, "@class"
        ).disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(mapper)));
    }

    @Bean
    public RedisCacheConfiguration byteCacheConfiguration(){
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new RedisSerializer<byte[]>() {
                            @Override
                            public byte[] serialize(byte[] bytes) {return bytes;}
                            @Override
                            public byte[] deserialize(byte[] bytes) {return bytes;}
                        }
                ));
    }

    /*@Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("thumbnails");
    }*/

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration jsonConfig = jsonCacheConfiguration();
        RedisCacheConfiguration byteConfig = byteCacheConfiguration();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(jsonConfig)
                .withCacheConfiguration("comments", jsonConfig)
                .withCacheConfiguration("thumbnails", byteConfig)
                .build();
    }
}