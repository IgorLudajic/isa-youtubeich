package com.team44.isa_youtubeich;

import com.team44.isa_youtubeich.domain.model.Comment;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.CommentRequestDto;
import com.team44.isa_youtubeich.exception.RateLimitExceededException;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.CommentService;
import com.team44.isa_youtubeich.util.CommentsRateLimiter;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CommentRateLimitTest {

    @Autowired
    private CommentsRateLimiter rateLimiter;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private Long videoId;


    @BeforeEach
    void setup(){
        User uploader = new User();
        uploader.setUsername("uploader");
        userRepository.save(uploader);

        Video video = new Video();
        video.setTitle("Testni video");
        video.setUser(uploader);
        videoId = videoRepository.save(video).getId();

        User prvi = new User();
        prvi.setUsername("prvi");
        userRepository.save(prvi);

        User drugi = new User();
        drugi.setUsername("drugi");
        userRepository.save(drugi);
    }

    @AfterEach
    void tearDown(){
        rateLimiter.reset();
    }

    @Test
    void testRateLimiting(){
        CommentRequestDto dto = new CommentRequestDto("Neki spam komentar.");

        User prvi = userRepository.findByUsername("prvi");
        User drugi = userRepository.findByUsername("drugi");

        for(int i = 0; i< 60; i++){
            commentService.postComment(videoId, dto, prvi.getUsername());
        }

        for(int i = 0; i < 30; i++){
            commentService.postComment(videoId, new CommentRequestDto("ovi mogu"), drugi.getUsername());
        }

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class, () -> {
            commentService.postComment(videoId, new CommentRequestDto("ovaj pada"), prvi.getUsername());
        });

        assertTrue(ex.getMessage().contains("Rate limit exceeded: 60 comments per hour."));

        try{
            Thread.sleep(6000);
        }
        catch(InterruptedException e) {}

        assertDoesNotThrow(() -> commentService.postComment(videoId, new CommentRequestDto("oladio se, sad open moze"), prvi.getUsername()));
    }
}
