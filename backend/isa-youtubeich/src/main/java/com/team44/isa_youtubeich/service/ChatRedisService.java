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

    private final String CHAT_CHANNEL = "global_chat_channel";
    private final String DELIMITER = "###DELIMITER###";
    @PostConstruct
    public void init() {
        redisContainer.addMessageListener(this, new ChannelTopic(CHAT_CHANNEL));
    }

    public void broadcastToCluster(ChatMessageDto message) {
        try {
            String rawMessage = message.getSender() + DELIMITER +
                    message.getContent() + DELIMITER +
                    message.getVideoId();

            redisTemplate.convertAndSend(CHAT_CHANNEL, rawMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Ručno vraćamo String u objekat (Deserijalizacija)
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            String[] parts = body.split(java.util.regex.Pattern.quote(DELIMITER));

            if (parts.length >= 3) {
                String sender = parts[0];
                String content = parts[1];
                String videoIdStr = parts[2];

                ChatMessageDto chatMessage = new ChatMessageDto();
                chatMessage.setSender(sender);
                chatMessage.setContent(content);
                chatMessage.setVideoId(Long.parseLong(videoIdStr));

                messagingTemplate.convertAndSend("/topic/video/" + chatMessage.getVideoId(), chatMessage);
            }
        } catch (Exception e) {
            System.err.println("Greska pri obradi Redis poruke: " + e.getMessage());
            e.printStackTrace();
        }
    }
}