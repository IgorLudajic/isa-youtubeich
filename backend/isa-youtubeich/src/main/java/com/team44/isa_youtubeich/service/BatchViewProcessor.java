package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.VideoView;
import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.repository.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class BatchViewProcessor implements SmartLifecycle {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private InstanceIdLeaseService instanceIdLeaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ScheduledExecutorService scheduler;
    private volatile boolean running;

    @Override
    public void start() {
        if (running) return;
        if (instanceIdLeaseService.getInstanceId().hashCode() % 10 == 0) { // Only one instance processes, based on UUID hash
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "batch-view-processor");
                t.setDaemon(true);
                return t;
            });
            scheduler.scheduleAtFixedRate(this::processBatch, 0, 10, TimeUnit.SECONDS); // Every 10 seconds
        }
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Start after other services
    }

    private void processBatch() {
        try {
            List<VideoView> views = new ArrayList<>();
            String json;
            while ((json = (String) redisTemplate.opsForList().rightPop("video:view_queue")) != null) {
                VideoView view = objectMapper.readValue(json, VideoView.class);
                views.add(view);
                if (views.size() >= 100) break; // Batch size 100
            }
            if (!views.isEmpty()) {
                videoViewRepository.saveAll(views);
            }
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
        }
    }
}
