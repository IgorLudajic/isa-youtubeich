package com.team44.isa_youtubeich.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class UserTokenStateDto {
    @Getter @Setter private String accessToken;
    @Getter @Setter private Long expiresIn;
}
