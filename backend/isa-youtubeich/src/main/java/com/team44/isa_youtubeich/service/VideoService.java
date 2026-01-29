package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.dto.VideoStreamResolutionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {

    Video uploadVideo(String title, String description, MultipartFile videoFile, MultipartFile thumbnailFile, String username, List<String> tags, String premieresAt, Double latitude, Double longitude) throws IOException;

    Page<VideoHomeDto> getPublicFeed(Pageable pageable);

    VideoDetailsDto getVideoDetails(Long id, String currentUsername);

    void incrementViews(Long id);

    byte[] getThumbnailContent(Long id);

    void startPremiere(Long id, String username);

    void endPremiere(Long id, String username);

    void cancelPremiere(Long id, String username);

    VideoStreamResolutionDto resolveStream(Long id);

    Video getVideoById(Long id);

    public void enqueueView(Long videoId, String username);
}
