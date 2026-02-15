package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailsDto {
    private Long Id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Long viewCount;
    private Long likes;
    private Long dislikes;
    private Boolean likedByCurrentUser;
    private Boolean dislikedByCurrentUser;
    private Date createdAt;
    private String creatorUsername;
    private LocalDateTime premieresAt;
    private Boolean isUpcoming;
    private Boolean isLive;
}
