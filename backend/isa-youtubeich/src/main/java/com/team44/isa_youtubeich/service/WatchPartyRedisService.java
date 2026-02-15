package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.WatchPartyEventDto;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Service
public class WatchPartyRedisService implements MessageListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisMessageListenerContainer redisContainer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String WP_CHANNEL = "watch_party_channel";

    @PostConstruct
    public void init(){
        redisContainer.addMessageListener(this, new ChannelTopic(WP_CHANNEL));
    }

    public void broadcastToCluster(WatchPartyEventDto event){
        try{
            String jsonMessage = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(WP_CHANNEL, jsonMessage);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try{
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            WatchPartyEventDto event = objectMapper.readValue(body, WatchPartyEventDto.class);

            messagingTemplate.convertAndSend("/topic/party/" + event.getPartyId(), event);
        }
        catch(Exception ex){
            System.err.println("Error processing Watch Party Redis message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
