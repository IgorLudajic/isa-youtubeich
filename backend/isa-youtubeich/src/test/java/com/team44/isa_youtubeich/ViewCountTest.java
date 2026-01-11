package com.team44.isa_youtubeich;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ViewCountTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    private Long videoId;

    @BeforeEach
    void setup() {
        videoRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");
        userRepository.save(user);

        Video video = new Video();
        video.setTitle("Concurrency Test Video");
        video.setViewCount(0L); // Počinjemo od nule
        video.setUser(user);

        Video savedVideo = videoRepository.save(video);
        videoId = savedVideo.getId();
    }

    @Test
    void testSynchronizedViewIncrement() throws InterruptedException {
        int numberOfThreads = 100; // 100 korisnika istovremeno
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Bazen od 10 niti

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

        Video updatedVideo = videoRepository.findById(videoId).orElseThrow();

        assertEquals(100L, updatedVideo.getViewCount());

        System.out.println("\nOčekivano pregleda: 100 | Dobijeno: " + updatedVideo.getViewCount() + "\n");
    }
}