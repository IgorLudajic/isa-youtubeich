package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.service.LivestreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/livestreams")
public class LivestreamController {

    @Autowired
    private LivestreamService livestreamService;

    @GetMapping("/{id}/playlist.m3u8")
    public ResponseEntity<Resource> getHLSPlaylist(@PathVariable Long id) {
        Path path = Paths.get(livestreamService.getHlsDirectory() + id + "/playlist.m3u8");
        Resource resource = new FileSystemResource(path);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/{id}/{segment}.ts")
    public ResponseEntity<Resource> getHLSSegment(@PathVariable Long id, @PathVariable String segment) {
        Path path = Paths.get(livestreamService.getHlsDirectory() + id + "/" + segment + ".ts");
        Resource resource = new FileSystemResource(path);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "video/MP2T");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
