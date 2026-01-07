package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicProfileDto {
    private String username;
    private String firstName;
    private String lastName;
    private Date createdAt;
    private Page<VideoHomeDto> videos;
}
