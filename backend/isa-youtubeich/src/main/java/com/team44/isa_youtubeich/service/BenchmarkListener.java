package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.UploadEventJsonDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class BenchmarkListener {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @RabbitListener(queues = "benchmark_queue")
    public void receiveMessage(byte[] message) {
        try {
            UploadEventJsonDto event = jsonMapper.readValue(message, UploadEventJsonDto.class);
            System.out.println("BenchmarkListener: primljena poruka za video: " + event.getVideoTitle());

        } catch (Exception e) {
            System.err.println("Greška pri čitanju poruke u Listeneru: " + e.getMessage());
        }
    }
}