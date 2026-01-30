package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.*;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.exception.ResourceConflictException;
import com.team44.isa_youtubeich.repository.*;
import com.team44.isa_youtubeich.service.VideoService;
import com.team44.isa_youtubeich.service.VideoViewService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private VideoViewService videoViewService;

    private final String UPLOAD_DIR = "uploads/";

    private final String API_BASE_URL = "http://localhost:8080/api/videos";


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Video uploadVideo(String title, String description, MultipartFile videoFile, MultipartFile thumbnailFile, String username, List<String> tags, String premieresAt, Double latitude, Double longitude) throws IOException {

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
            video.setLikes(0L);
            video.setDislikes(0L);

            video.setTags(tags);
            video.setFileSize(FileSize.of(videoFile.getSize()));

            if (premieresAt != null && !premieresAt.isBlank()) {
                LocalDateTime localDateTime = LocalDateTime.parse(premieresAt);
                video.setPremieresAt(localDateTime);
            } else {
                video.setPremieresAt(null);
            }
            if (latitude != null && longitude != null) {
                video.setLocation(new GeoLocation(latitude, longitude));
            } else {
                video.setLocation(null);
            }

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
        LocalDateTime now = LocalDateTime.now();

        return videoRepository.findAllReleasedVideos(now, pageable)
                .map(video -> new VideoHomeDto(
                        video.getId(),
                        video.getTitle(),
                        // IZMENA: Vraćamo URL ka kontroleru, ne putanju sa diska!
                        API_BASE_URL + "/" + video.getId() + "/thumbnail",
                        videoViewService.getViewCount(video.getId()),
                        video.getLikes(),
                        video.getDislikes(),
                        Date.from(video.getCreatedAt().toInstant()),
                        video.getUser().getUsername()
                ));
    }

    @Override
    public VideoDetailsDto getVideoDetails(Long id, String currentUsername){

        Video video = videoRepository.findById(id).orElseThrow(() -> new ResourceConflictException(id, "Video not found"));

        VideoDetailsDto dto = new VideoDetailsDto(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                // IZMENA: Vraćamo URL ka kontroleru
                API_BASE_URL + "/" + video.getId() + "/thumbnail",
                videoViewService.getViewCount(video.getId()),
                video.getLikes(),
                video.getDislikes(),
                false,
                false,
                Date.from(video.getCreatedAt().toInstant()),
                video.getUser().getUsername()
        );

        if(currentUsername != null){
            Reaction reaction = reactionRepository.findByVideoIdAndUserUsername(id, currentUsername);
            if(reaction != null){
                if(reaction.getType() == ReactionType.LIKE)
                    dto.setLikedByCurrentUser(true);
                else
                    dto.setDislikedByCurrentUser(true);
            }
        }

        return dto;
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

    @Override
    @Cacheable("thumbnails")
    public byte[] getThumbnailContent(Long id) {
        // System.out.println("DISK OPERACIJA: Učitavam sliku " + id + " sa hard diska...");

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video nije pronađen"));

        try {
            Path path = Paths.get(video.getThumbnailUrl());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Greška pri čitanju thumbnail-a: " + video.getThumbnailUrl());
        }
    }

    @Override
    public void enqueueView(Long videoId, String username) {
        videoViewService.enqueueView(videoId, username);
    }
}
