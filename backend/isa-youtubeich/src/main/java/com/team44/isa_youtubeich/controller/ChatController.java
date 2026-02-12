package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.ChatMessageDto; // Koristimo DTO
import com.team44.isa_youtubeich.service.ChatRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatRedisService chatRedisService;

    @MessageMapping("/chat/{videoId}")
    public void sendMessage(@DestinationVariable Long videoId, @Payload ChatMessageDto chatMessage) {
        // Lombok je generisao settere, pa ovo radi:
        chatMessage.setVideoId(videoId);

        chatRedisService.broadcastToCluster(chatMessage);
    }
}