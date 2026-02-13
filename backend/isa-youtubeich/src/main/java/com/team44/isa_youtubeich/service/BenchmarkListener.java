package com.team44.isa_youtubeich.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BenchmarkListener {

    @RabbitListener(queues = "benchmark_queue")
    public void receiveMessage(byte[] message) {
    }
}