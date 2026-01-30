package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.DailyVideoPopularity;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.VideoPopularityDto;
import com.team44.isa_youtubeich.repository.DailyVideoPopularityRepository;
import com.team44.isa_youtubeich.service.VideoPopularityService;
import com.team44.isa_youtubeich.service.VideoViewService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/popularity")
public class VideoPopularityController {

    @Autowired
    private DailyVideoPopularityRepository dailyVideoPopularityRepository;

    @Autowired
    private VideoPopularityService videoPopularityService;

    @Autowired
    private VideoViewService videoViewService;

    @GetMapping("/latest")
    public ResponseEntity<List<VideoPopularityDto>> getLatestPopularity(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<DailyVideoPopularity> latest = dailyVideoPopularityRepository.findTopByOrderByRunTimestampDesc();
        if (latest.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        DailyVideoPopularity pop = latest.get();
        List<VideoPopularityDto> result = new ArrayList<>();

        if (pop.getTop1Video() != null) {
            VideoPopularityDto dto = getVideoPopularityDto(pop.getTop1Video(), pop.getTop1Score());
            result.add(dto);
        }

        if (pop.getTop2Video() != null) {
            VideoPopularityDto dto = getVideoPopularityDto(pop.getTop2Video(), pop.getTop2Score());
            result.add(dto);
        }

        if (pop.getTop3Video() != null) {
            VideoPopularityDto dto = getVideoPopularityDto(pop.getTop3Video(), pop.getTop3Score());
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    private @NonNull VideoPopularityDto getVideoPopularityDto(Video pop, Double pop1) {
        VideoPopularityDto dto = new VideoPopularityDto();
        dto.setVideoId(pop.getId());
        dto.setTitle(pop.getTitle());
        dto.setThumbnailUrl("/api/videos/" + pop.getId() + "/thumbnail");
        dto.setViewCount(videoViewService.getViewCount(pop.getId()));
        dto.setLikes(pop.getLikes());
        dto.setDislikes(pop.getDislikes());
        dto.setCreatedAt(pop.getCreatedAt());
        dto.setCreatorUsername(pop.getUser().getUsername());
        dto.setScore(pop1);
        return dto;
    }

    @PostMapping("/run")
    public ResponseEntity<Void> forceRunPipeline(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        videoPopularityService.calculateAndSavePopularity();
        return ResponseEntity.ok().build();
    }
}
