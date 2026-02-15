package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.ChatMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Service
public class ChatRedisService implements MessageListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RedisMessageListenerContainer redisContainer;

    // 👇 KORISTIMO JsonMapper UMESTO ObjectMapper
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final String CHAT_CHANNEL = "global_chat_channel";

    @PostConstruct
    public void init() {
        redisContainer.addMessageListener(this, new ChannelTopic(CHAT_CHANNEL));
    }

    public void broadcastToCluster(ChatMessageDto message) {
        try {
            // FIX: Pretvaramo objekat u JSON string
            String jsonMessage = jsonMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(CHAT_CHANNEL, jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            // FIX: Vraćamo JSON string nazad u Java objekat
            ChatMessageDto chatMessage = jsonMapper.readValue(body, ChatMessageDto.class);

            messagingTemplate.convertAndSend("/topic/video/" + chatMessage.getVideoId(), chatMessage);

        } catch (Exception e) {
            System.err.println("Greska pri obradi Redis poruke: " + e.getMessage());
            e.printStackTrace();
        }
    }
}