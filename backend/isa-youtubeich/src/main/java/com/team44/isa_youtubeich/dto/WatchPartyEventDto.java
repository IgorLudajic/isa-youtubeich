package com.team44.isa_youtubeich.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyEventDto {
    private String type;
    private Long videoId;
    private Long partyId;
}
