package com.team44.isa_youtubeich.config;

import com.team44.isa_youtubeich.service.LivestreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PremiereMessageListener implements MessageListener {

    public static final String TOPIC_NAME = "premiere-schedule";

    @Autowired
    private LivestreamService livestreamService;

    @Autowired
    private TaskScheduler taskScheduler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        String[] parts = body.split(":");
        Long videoId = Long.parseLong(parts[0]);
        LocalDateTime premieresAt = LocalDateTime.parse(parts[1]);

        // Schedule the task at premieresAt
        taskScheduler.schedule(() -> livestreamService.startPremiere(videoId), premieresAt.atZone(java.time.ZoneOffset.UTC).toInstant());
    }
}
