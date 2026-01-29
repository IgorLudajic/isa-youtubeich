package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.*;
import com.team44.isa_youtubeich.dto.VideoDetailsDto;
import com.team44.isa_youtubeich.dto.VideoHomeDto;
import com.team44.isa_youtubeich.dto.VideoStreamResolutionDto;
import com.team44.isa_youtubeich.exception.ResourceConflictException;
import com.team44.isa_youtubeich.repository.CommentRepository;
import com.team44.isa_youtubeich.repository.ReactionRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.LivestreamService;
import com.team44.isa_youtubeich.service.VideoService;
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
    private LivestreamService livestreamService;

    private final String UPLOAD_DIR = "uploads/";
    private final String VIDEO_DIR = "uploads/videos/";

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
            createVideoDirectoryIfNotExists();

            // Save thumbnail immediately (we want it even if video transcoding fails later)
            String savedThumbStr = saveFileToDisk(thumbnailFile);
            thumbnailPath = Paths.get(savedThumbStr);

            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setThumbnailUrl(savedThumbStr);
            video.setUser(user);
            video.setViewCount(0L);
            video.setLikes(0L);
            video.setDislikes(0L);

            video.setTags(tags);
            video.setFileSize(FileSize.of(videoFile.getSize()));

            if (premieresAt != null && !premieresAt.isBlank()) {
                LocalDateTime localDateTime = LocalDateTime.parse(premieresAt);
                video.setPremieresAt(localDateTime);
                video.setStatus(VideoStatus.SCHEDULED);
            } else {
                video.setPremieresAt(null);
                video.setStatus(VideoStatus.ENDED);
            }
            if (latitude != null && longitude != null) {
                video.setLocation(new GeoLocation(latitude, longitude));
            } else {
                video.setLocation(null);
            }

            Video savedVideo = videoRepository.save(video);

            // Store MP4 under predictable static path.
            String staticVideoPath = VIDEO_DIR + savedVideo.getId() + ".mp4";
            Path staticPath = Paths.get(staticVideoPath);
            Files.copy(videoFile.getInputStream(), staticPath, StandardCopyOption.REPLACE_EXISTING);
            videoPath = staticPath;

            savedVideo.setVideoUrl(staticVideoPath);
            savedVideo = videoRepository.save(savedVideo);

            if (savedVideo.getStatus() == VideoStatus.SCHEDULED) {
                livestreamService.schedulePremiere(savedVideo.getId(), savedVideo.getPremieresAt());
            }

            return savedVideo;

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

    private void createVideoDirectoryIfNotExists() throws IOException {
        Path videoDir = Paths.get(VIDEO_DIR);
        if (!Files.exists(videoDir)) {
            Files.createDirectories(videoDir);
        }
    }

    @Override
    public Page<VideoHomeDto> getPublicFeed(Pageable pageable){
        return videoRepository.findAllForHomeFeed(pageable)
                .map(video -> new VideoHomeDto(
                        video.getId(),
                        video.getTitle(),
                        // IZMENA: Vraćamo URL ka kontroleru, ne putanju sa diska!
                        API_BASE_URL + "/" + video.getId() + "/thumbnail",
                        video.getViewCount(),
                        video.getLikes(),
                        video.getDislikes(),
                        Date.from(video.getCreatedAt().toInstant()),
                        video.getUser().getUsername(),
                        video.getStatus() == VideoStatus.SCHEDULED,
                        video.getStatus() == VideoStatus.LIVE,
                        video.getPremieresAt()
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
                video.getViewCount(),
                video.getLikes(),
                video.getDislikes(),
                false,
                false,
                Date.from(video.getCreatedAt().toInstant()),
                video.getUser().getUsername(),
                video.getPremieresAt(),
                video.getStatus() == VideoStatus.LIVE
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

    @Override
    @Transactional
    public void incrementViews(Long id) {
        videoRepository.incrementViewCount(id);
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
    public void startPremiere(Long id, String username) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        if (!username.equals(video.getUser().getUsername())) {
            throw new RuntimeException("Not authorized");
        }
        livestreamService.startPremiereEarly(id);
    }

    @Override
    public void endPremiere(Long id, String username) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        if (!username.equals(video.getUser().getUsername())) {
            throw new RuntimeException("Not authorized");
        }
        livestreamService.endPremiere(id);
    }

    @Override
    public void cancelPremiere(Long id, String username) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        if (!username.equals(video.getUser().getUsername())) {
            throw new RuntimeException("Not authorized");
        }
        livestreamService.cancelPremiere(id);
    }

    @Override
    public VideoStreamResolutionDto resolveStream(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException(id, "Video not found"));

        // Expected asset locations (internal, controlled paths)
        Path hlsPlaylistPath = Paths.get(livestreamService.getHlsDirectory(), String.valueOf(id), "playlist.m3u8");
        String hlsPlaylistUrl = "/uploads/hls/" + id + "/playlist.m3u8";

        Path mp4Path = Paths.get(VIDEO_DIR, id + ".mp4");
        // IMPORTANT: MP4 must be served via controller to enforce VideoStatus.ENDED.
        String mp4Url = API_BASE_URL + "/" + id + "/mp4";

        boolean hlsExists = Files.exists(hlsPlaylistPath);
        boolean mp4Exists = Files.exists(mp4Path);

        // Decide based on domain status first.
        if (video.getStatus() == VideoStatus.LIVE) {
            if (hlsExists) {
                return VideoStreamResolutionDto.available(VideoStreamResolutionDto.StreamKind.HLS, hlsPlaylistUrl, video.getStatus());
            }
            // Live, but playlist not there yet (ffmpeg startup delay or failure)
            return VideoStreamResolutionDto.notReady(video.getStatus(), "Live stream is starting");
        }

        if (video.getStatus() == VideoStatus.SCHEDULED) {
            // Gate playback behind the premiere.
            return VideoStreamResolutionDto.notReady(video.getStatus(), "Premiere has not started");
        }

        // ENDED (or any other non-live) -> prefer VOD mp4
        if (mp4Exists) {
            return VideoStreamResolutionDto.available(VideoStreamResolutionDto.StreamKind.VOD, mp4Url, video.getStatus());
        }

        // Unexpected: ended but mp4 missing. As a last-resort, if HLS exists (cleanup failed), serve it.
        if (hlsExists) {
            return VideoStreamResolutionDto.available(VideoStreamResolutionDto.StreamKind.HLS, hlsPlaylistUrl, video.getStatus());
        }

        return VideoStreamResolutionDto.missing(video.getStatus(), "No stream assets found");
    }

    @Override
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException(id, "Video not found"));
    }
}
