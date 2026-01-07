package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.dto.CommentResponseDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.repository.CommentRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.VideoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Video uploadVideo(String title, String description, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException {

        if (videoFile == null || videoFile.isEmpty()) {
            throw new RuntimeException("Video fajl je obavezan! Niste izabrali fajl.");
        }
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new RuntimeException("Thumbnail slika je obavezna! Niste izabrali fajl.");
        }

        Path videoPath = null;
        Path thumbnailPath = null;

        try {
            User user = userRepository.findByUsername(username);
            if (user == null) throw new RuntimeException("User not found");
            createUploadDirectoryIfNotExists();

            String savedVideoStr = saveFileToDisk(videoFile);
            videoPath = Paths.get(savedVideoStr);

            String savedThumbStr = saveFileToDisk(thumbnailFile);
            thumbnailPath = Paths.get(savedThumbStr);

            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setVideoUrl(savedVideoStr);
            video.setThumbnailUrl(savedThumbStr);
            video.setUser(user);

            return videoRepository.save(video);

        } catch (Exception e) {
            if (videoPath != null) {
                Files.deleteIfExists(videoPath);
            }
            if (thumbnailPath != null) {
                Files.deleteIfExists(thumbnailPath);
            }
            throw e;
        }
    }
    @Override
    public Video getVideoAndIncrementViews(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video nije pronađen"));

        video.setViewCount(video.getViewCount() + 1);

        return videoRepository.save(video);
    }

    @Override
    public byte[] getVideoContent(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video nije pronađen"));

        try {
            Path path = Paths.get(video.getVideoUrl());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Greška: Ne mogu da pročitam fajl sa lokacije: " + video.getVideoUrl());
        }
    }

    @Override
    public Page<VideoHomeDto> getPublicFeed(Pageable pageable){
        return videoRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(video -> new VideoHomeDto(
                        video.getId(),
                        video.getTitle(),
                        video.getThumbnailUrl(),
                        video.getViewCount(),
                        Date.from(video.getCreatedAt().toInstant()),
                        video.getUser().getUsername()
                ));
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}