package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.crdt.VideoCounter;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoView;
import com.team44.isa_youtubeich.instance.InstanceIdLeaseService;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.repository.VideoViewRepository;
import com.team44.isa_youtubeich.service.VideoViewService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private VideoViewRepository videoViewRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<Long, VideoCounter> videoCounters = new ConcurrentHashMap<>();

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
            // Use VideoCounter for immediate view count
            getVideoCounter(videoId).increment();
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue view", e);
        }
    }

    @Override
    public Long getViewCount(Long videoId) {
        return getVideoCounter(videoId).getValue();
    }

    private VideoCounter getVideoCounter(Long videoId) {
        return videoCounters.computeIfAbsent(videoId, id -> {
            String channel = "gcounter:video:" + id;
            VideoCounter videoCounter = new VideoCounter(stringRedisTemplate, instanceIdLeaseService, redisMessageListenerContainer, channel, id, videoViewRepository, videoRepository);
            videoCounter.init();
            return videoCounter;
        });
    }
}
