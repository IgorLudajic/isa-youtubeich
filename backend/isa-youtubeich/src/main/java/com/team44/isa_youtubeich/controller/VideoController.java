package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoStatus;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.dto.VideoStreamResolutionDto;
import com.team44.isa_youtubeich.service.LivestreamService;
import com.team44.isa_youtubeich.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private LivestreamService livestreamService;

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

    @GetMapping("/{id}/stream")
    public ResponseEntity<Void> streamVideo(@PathVariable Long id) {
        VideoStreamResolutionDto resolution = videoService.resolveStream(id);

        if (resolution.getAvailability() == VideoStreamResolutionDto.Availability.AVAILABLE) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(resolution.getLocation()))
                    .build();
        }

        if (resolution.getAvailability() == VideoStreamResolutionDto.Availability.NOT_READY) {
            // Premiere/live stream exists conceptually, but isn't ready to play yet.
            // 409 makes it explicit vs a generic 404.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getThumbnailContent(id));
    }

    @PostMapping("/{id}/startPremiere")
    public ResponseEntity<?> startPremiere(@PathVariable Long id, Principal principal) {
        try {
            String username = principal.getName();
            videoService.startPremiere(id, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/endPremiere")
    public ResponseEntity<?> endPremiere(@PathVariable Long id, Principal principal) {
        try {
            String username = principal.getName();
            videoService.endPremiere(id, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancelPremiere")
    public ResponseEntity<?> cancelPremiere(@PathVariable Long id, Principal principal) {
        try {
            String username = principal.getName();
            videoService.cancelPremiere(id, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/mp4")
    public ResponseEntity<Resource> getMp4(@PathVariable Long id) {
        // This endpoint intentionally enforces VideoStatus.ENDED.
        Video video = videoService.getVideoById(id);

        if (video.getStatus() != VideoStatus.ENDED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Path mp4Path = Paths.get(video.getVideoUrl()).toAbsolutePath().normalize();
            if (!Files.exists(mp4Path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(mp4Path.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}