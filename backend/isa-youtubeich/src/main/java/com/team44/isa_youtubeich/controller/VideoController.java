package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.CommentResponseDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam("thumbnailFile") MultipartFile thumbnailFile) {

        try {
            String username = "user";

            Video savedVideo = videoService.uploadVideo(title, description, videoFile, thumbnailFile, username);
            return ResponseEntity.ok("Video uspešno postavljen! ID: " + savedVideo.getId());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Greška prilikom čitanja fajla");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoInfo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoAndIncrementViews(id));
    }

    @GetMapping(value = "/{id}/stream", produces = "video/mp4")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoContent(id));
    }

    @GetMapping
    public ResponseEntity<Page<VideoHomeDto>> getHomeFeed(Pageable pageable){
        return ResponseEntity.ok(videoService.getPublicFeed(pageable));
    }
}