package com.team44.isa_youtubeich.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthRequestDto {
    @Getter @Setter private String username;
    @Getter @Setter private String password;
}
