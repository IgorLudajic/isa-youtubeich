package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.crdt.GCounter;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoView;
import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.VideoViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VideoViewServiceImpl implements VideoViewService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private InstanceIdLeaseService instanceIdLeaseService;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Value("${app.instance-id.max-instances:16}")
    private int maxInstances;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<Long, GCounter> gCounters = new ConcurrentHashMap<>();

    @Override
    public void enqueueView(Long videoId, String username) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username);
        }

        VideoView view = new VideoView();
        view.setViewedAt(new Timestamp(System.currentTimeMillis()));
        view.setViewedFrom(null); // TODO: extract from user IP/profile
        view.setUser(user);
        view.setVideo(video);

        try {
            String json = objectMapper.writeValueAsString(view);
            redisTemplate.opsForList().leftPush("video:view_queue", json);
            // Use GCounter for immediate view count
            getGCounter(videoId).increment();
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue view", e);
        }
    }

    @Override
    public Long getViewCount(Long videoId) {
        return getGCounter(videoId).getValue();
    }

    private GCounter getGCounter(Long videoId) {
        return gCounters.computeIfAbsent(videoId, id -> {
            String channel = "gcounter:video:" + id;
            GCounter gCounter = new GCounter(stringRedisTemplate, instanceIdLeaseService, redisMessageListenerContainer, maxInstances, channel);
            gCounter.init();
            return gCounter;
        });
    }
}
