package com.team44.isa_youtubeich.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class VideoPopularityDto {
    private Long videoId;
    private String title;
    private String thumbnailUrl;
    private Long viewCount;
    private Long likes;
    private long dislikes;
    private Date createdAt;
    private String creatorUsername;
    private Double score;
}
