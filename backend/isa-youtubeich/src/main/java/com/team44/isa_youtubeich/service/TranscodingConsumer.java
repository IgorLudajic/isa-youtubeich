package com.team44.isa_youtubeich.service;


import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoStatus;
import com.team44.isa_youtubeich.dto.TranscodingJobDto;
import com.team44.isa_youtubeich.repository.VideoRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TranscodingConsumer {
    private static final Logger log = LoggerFactory.getLogger(TranscodingConsumer.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private LivestreamService livestreamService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isRunning = true;

    @PostConstruct
    public void startListener(){
        executor.submit(this::listenForJobs);
    }


    private void listenForJobs(){
        log.info("Started transcoding consumer...");

        while(isRunning){
            try{
                Object message = redisTemplate.opsForList().leftPop("transcoding:queue", 5, TimeUnit.SECONDS);

                if(message != null) {
                    String jsonString = (String)message;
                    TranscodingJobDto job = objectMapper.readValue(jsonString, TranscodingJobDto.class);
                    processTranscoding(job);
                }
            }
            catch(Exception ex){
                log.error("Error in transcoding listener loop", ex);
            }
        }
    }


    private void processTranscoding(TranscodingJobDto job){
        log.info("Starting transcoding for video ID: {}", job.getVideoId());

        try{
            String inputPath = job.getInputPath();
            String outputPath = inputPath.replace(".mp4", "_optimized.mp4");

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-vf", "scale=-2:144",
                    "-b:v", "100k",
                    "-c:a", "aac",
                    "-b:a", "8k",
                    outputPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            int exitCode = process.waitFor();

            if(exitCode == 0){
                log.info("Transcoding finished successfully for video ID: {}", job.getVideoId());

                Path original = Paths.get(inputPath);
                Path optimized = Paths.get(outputPath);

                Files.move(optimized, original, StandardCopyOption.REPLACE_EXISTING);

                finalizeVideoStatus(job.getVideoId());
            }
            else{
                log.error("Transcoding failed with exit code: {}", exitCode);
            }

        }
        catch(Exception ex){
            log.error("Transcoding failed", ex);
        }
    }

    private void finalizeVideoStatus(Long videoId){
        Video video = videoRepository.findById(videoId).orElse(null);
        if(video == null) return;

        if(video.getPremieresAt() != null){
            video.setStatus(VideoStatus.SCHEDULED);
            videoRepository.save(video);

            livestreamService.schedulePremiere(video.getId(), video.getPremieresAt());
            log.info("Video {} set to SCHEDULED", videoId);
        }
        else{
            video.setStatus(VideoStatus.ENDED);
            videoRepository.save(video);
            log.info("Video {} set to ENDED (Published)", videoId);
        }
    }
}
