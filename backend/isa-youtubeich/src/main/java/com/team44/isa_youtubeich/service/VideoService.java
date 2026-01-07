package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface VideoService {

    Video uploadVideo(String title, String description, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException;

    Video getVideoAndIncrementViews(Long id);

    byte[] getVideoContent(Long id);

    Page<VideoHomeDto> getPublicFeed(Pageable pageable);

    VideoDetailsDto getVideoDetailsAndIncrementViews(Long id, String currentUsername);
}