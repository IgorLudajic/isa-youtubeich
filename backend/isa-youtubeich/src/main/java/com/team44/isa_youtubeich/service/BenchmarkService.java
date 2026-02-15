package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.UploadEventJsonDto;
import com.team44.isa_youtubeich.protobuf.UploadEventProto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BenchmarkService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String QUEUE_NAME = "benchmark_queue";
    private final int MESSAGE_COUNT = 50;

    public String runBenchmark() {
        StringBuilder report = new StringBuilder();
        report.append("POČINJEM DETALJAN BENCHMARK (JSON vs PROTOBUF)...\n");

        List<UploadEventJsonDto> events = new ArrayList<>();
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            events.add(new UploadEventJsonDto("Video " + i, 1024L + i, "User" + i, System.currentTimeMillis()));
        }

        long jsonTotalSerTime = 0;
        long jsonTotalDeserTime = 0;
        long jsonTotalSize = 0;

        for (UploadEventJsonDto event : events) {
            try {
                long startSer = System.nanoTime();
                String jsonString = String.format(
                        "{\"videoTitle\":\"%s\",\"fileSize\":%d,\"author\":\"%s\",\"timestamp\":%d}",
                        event.getVideoTitle(), event.getFileSize(), event.getAuthor(), event.getTimestamp()
                );
                byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                long endSer = System.nanoTime();

                jsonTotalSerTime += (endSer - startSer);
                jsonTotalSize += bytes.length;

                long startDeser = System.nanoTime();
                String parsedString = new String(bytes, StandardCharsets.UTF_8);
                String[] parts = parsedString.split(",");
                long endDeser = System.nanoTime();

                jsonTotalDeserTime += (endDeser - startDeser);
                rabbitTemplate.convertAndSend(QUEUE_NAME, bytes);
            } catch (Exception e) { e.printStackTrace(); }
        }

        long protoTotalSerTime = 0;
        long protoTotalDeserTime = 0;
        long protoTotalSize = 0;

        for (UploadEventJsonDto event : events) {
            try {
                long startSer = System.nanoTime();
                UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.newBuilder()
                        .setVideoTitle(event.getVideoTitle())
                        .setFileSize(event.getFileSize())
                        .setAuthor(event.getAuthor())
                        .setTimestamp(event.getTimestamp())
                        .build();
                byte[] bytes = protoEvent.toByteArray();
                long endSer = System.nanoTime();

                protoTotalSerTime += (endSer - startSer);
                protoTotalSize += bytes.length;

                long startDeser = System.nanoTime();
                UploadEventProto.UploadEvent.parseFrom(bytes);
                long endDeser = System.nanoTime();

                protoTotalDeserTime += (endDeser - startDeser);
                rabbitTemplate.convertAndSend(QUEUE_NAME, bytes);
            } catch (Exception e) { e.printStackTrace(); }
        }


        double avgJsonSer = (jsonTotalSerTime / (double) MESSAGE_COUNT) / 1000.0;
        double avgJsonDeser = (jsonTotalDeserTime / (double) MESSAGE_COUNT) / 1000.0;
        double avgJsonSize = jsonTotalSize / (double) MESSAGE_COUNT;

        double avgProtoSer = (protoTotalSerTime / (double) MESSAGE_COUNT) / 1000.0;
        double avgProtoDeser = (protoTotalDeserTime / (double) MESSAGE_COUNT) / 1000.0;
        double avgProtoSize = protoTotalSize / (double) MESSAGE_COUNT;

        report.append("==========================================\n");
        report.append("REZULTATI (Prosek na " + MESSAGE_COUNT + " poruka)\n");
        report.append("==========================================\n");
        report.append(String.format("JSON Serijalizacija:   %.3f µs\n", avgJsonSer));
        report.append(String.format("PROTO Serijalizacija:  %.3f µs\n", avgProtoSer));
        report.append("------------------------------------------\n");
        report.append(String.format("JSON Deserijalizacija: %.3f µs\n", avgJsonDeser));
        report.append(String.format("PROTO Deserijalizacija:%.3f µs\n", avgProtoDeser));
        report.append("------------------------------------------\n");
        report.append(String.format("JSON Veličina:         %.0f bajtova\n", avgJsonSize));
        report.append(String.format("PROTO Veličina:        %.0f bajtova\n", avgProtoSize));
        report.append("------------------------------------------\n");

        double sizeReduction = (1.0 - avgProtoSize / avgJsonSize) * 100;
        report.append(String.format("🏆 Protobuf je manji za %.2f%%\n", sizeReduction));

        if (avgProtoSer < avgJsonSer) {
            double speedUp = avgJsonSer / avgProtoSer;
            report.append(String.format("Protobuf je %.2fx brži u serijalizaciji\n", speedUp));
        } else {
            double speedUp = avgProtoSer / avgJsonSer;
            report.append(String.format("JSON je %.2fx brži (kod ultra-malih poruka)\n", speedUp));
        }

        return report.toString();
    }
}