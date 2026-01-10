package com.team44.isa_youtubeich;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class VideoConcurrencyTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    public void testConcurrentViewIncrements() throws InterruptedException {
        Long videoId = 1L;

        Video v = videoRepository.findById(videoId).get();
        v.setViewCount(0L);
        videoRepository.save(v);

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    videoService.incrementViews(videoId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        Video updatedVideo = videoRepository.findById(videoId).get();
        System.out.println("Očekivano: " + numberOfThreads);
        System.out.println("Stvarno stanje u bazi: " + updatedVideo.getViewCount());

        assertEquals(numberOfThreads, updatedVideo.getViewCount());
    }
}
