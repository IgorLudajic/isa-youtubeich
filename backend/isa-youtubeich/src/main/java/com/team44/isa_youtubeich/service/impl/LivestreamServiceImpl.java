package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.config.PremiereMessageListener;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoStatus;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.LivestreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class LivestreamServiceImpl implements LivestreamService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    private static final String HLS_DIR = "uploads/hls/";

    @Override
    public String getHlsDirectory() { return HLS_DIR; }

    @Override
    public void schedulePremiere(Long videoId, LocalDateTime premieresAt) {
        // Schedule on current instance
        taskScheduler.schedule(() -> startPremiere(videoId), premieresAt.atZone(ZoneOffset.UTC).toInstant());

        // Publish to Redis for other instances
        redisTemplate.convertAndSend(PremiereMessageListener.TOPIC_NAME, videoId + ":" + premieresAt);
    }

    @Override
    public void startPremiere(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();
        if (video.getStatus() != VideoStatus.SCHEDULED) return;

        video.setStatus(VideoStatus.LIVE);
        videoRepository.save(video);

        // Start transcoding
        transcodeToHLS(video);
    }

    @Override
    public void endPremiere(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        video.setStatus(VideoStatus.ENDED);
        videoRepository.save(video);

        // Stop transcoding if running
        // For now, assume it ends when video ends
    }

    private void transcodeToHLS(Video video) {
        try {
            Path hlsPath = Paths.get(HLS_DIR + video.getId());
            Files.createDirectories(hlsPath);

            String inputPath = video.getVideoUrl();
            String outputPath = hlsPath.toString() + "/playlist.m3u8";

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputPath,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-hls_time", "10",
                "-hls_list_size", "10",
                "-hls_flags", "delete_segments",
                "-f", "hls",
                outputPath
            );
            pb.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
