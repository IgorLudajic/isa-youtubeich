package com.team44.isa_youtubeich.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BenchmarkListener {

    // Sluša JSON poruke (ako si u BenchmarkService slao na "benchmark_queue")
    @RabbitListener(queues = "benchmark_queue")
    public void receiveMessage(byte[] message) {
        // Samo logujemo da je stiglo, ne radimo ništa pametno da ne usporimo test
        // System.out.println("📩 Stigla poruka, veličina: " + message.length + " bajtova");
    }
}