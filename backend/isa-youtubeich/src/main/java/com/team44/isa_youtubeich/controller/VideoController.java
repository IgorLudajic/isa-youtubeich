package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
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
import java.security.Principal;
import java.util.List;

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
            @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "premieresAt", required = false) String premieresAt,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            Principal principal) {

        try {
            String username = principal.getName();

            Video savedVideo = videoService.uploadVideo(title, description, videoFile, thumbnailFile, username, tags, premieresAt, latitude, longitude);

            return ResponseEntity.ok("Video uspešno postavljen! ID: " + savedVideo.getId());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<VideoHomeDto>> getHomeFeed(Pageable pageable){
        return ResponseEntity.ok(videoService.getPublicFeed(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<VideoDetailsDto> getVideoInfo(@PathVariable Long id, Principal principal){
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(videoService.getVideoDetails(id, username));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> viewVideo(@PathVariable Long id) {
        videoService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/stream", produces = "video/mp4")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoContent(id));
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getThumbnailContent(id));
    }
}