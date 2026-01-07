package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoHomeDto {
    private Long Id;
    private String title;
    private String thumbnailUrl;
    private Long viewCount;
    private Long likes;
    private long dislikes;
    private Date createdAt;
    private String creatorUsername;
}
