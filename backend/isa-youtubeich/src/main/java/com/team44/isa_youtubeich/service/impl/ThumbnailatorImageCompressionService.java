package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.ImageCompressionService;
import jakarta.annotation.PostConstruct;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ThumbnailatorImageCompressionService implements ImageCompressionService {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailatorImageCompressionService.class);
    private static final String COMPRESSED_SUFFIX = "_compressed";
    private static final String LOCK_KEY = "thumbnail:compression:scheduler:lock";

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isRunning = true;

    @PostConstruct
    public void startWorker(){
        executor.submit(this::listenForJobs);
    }

    private void listenForJobs(){
        log.info("Started thumbnail compression worker...");
        while(isRunning){
            try{
                Object message = redisTemplate.opsForList().leftPop("image:compression:queue", 5, TimeUnit.SECONDS);

                if(message != null){
                    Long videoId = Long.valueOf(message.toString());
                    processCompression(videoId);
                }
            }
            catch(Exception ex){
                log.error("Error in thumbnail compression worker loop: ", ex);
            }
        }
    }

    private void processCompression(Long videoId){
        try{
            Video video = videoRepository.findById(videoId).orElse(null);

            if(video == null) return;
            if(video.getThumbnailUrl().contains(COMPRESSED_SUFFIX)) return;

            log.info("Processing thumbnail compression for video id: {}", videoId);

            Path originalPath = Paths.get(video.getThumbnailUrl());
            File originalFile = originalPath.toFile();

            if(!originalFile.exists()) return;

            String newFilename = originalPath.getFileName().toString();
            String fileExtension = "";
            int dotIndex = newFilename.lastIndexOf('.');
            if(dotIndex > 0){
                fileExtension = newFilename.substring(dotIndex);
                newFilename = newFilename.substring(0, dotIndex);
            }
            newFilename += "_compressed" + fileExtension;

            Path newPath = originalPath.getParent().resolve(newFilename);
            File newFile = newPath.toFile();

            Thumbnails.of(originalFile).scale(1.0).outputQuality(0.5).toFile(newFile);

            video.setThumbnailUrl(newPath.toString());
            videoRepository.save(video);

            log.info("Thumbnail compression complete for video id: {}", videoId);
        }
        catch(IOException ex){
            log.error("Failed to compress thumbnail for video id: {}", videoId, ex);
        }
    }

    @Scheduled(cron = "0 8 20 * * ?")
    public void scheduleCompressionTasks(){
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCKED", Duration.ofMinutes(5));

        if(acquired){
            log.info("Acquired lock for thumbnail compression scheduling. Finding jobs...");

            try{
                LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                Timestamp cutoffDate = Timestamp.valueOf(oneMonthAgo);

                List<Video> videos = videoRepository.findByCreatedAtBeforeAndThumbnailUrlNotContaining(cutoffDate, COMPRESSED_SUFFIX);

                for(Video v : videos){
                    redisTemplate.opsForList().rightPush("image:compression:queue", v.getId().toString());
                }

                log.info("Scheduled {} images for compression.", videos.size());
            }
            finally{
                redisTemplate.delete(LOCK_KEY);
            }
        }
        else{
            log.info("Failed to acquire lock for thumbnail compression scheduling. Listening for jobs...");
        }
    }
}
