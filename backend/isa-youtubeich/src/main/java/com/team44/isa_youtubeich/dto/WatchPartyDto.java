package com.team44.isa_youtubeich.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyDto {
    private Long id;
    private String name;
    private String ownerUsername;

    @JsonProperty("isOwner")
    private boolean isOwner;

    @JsonProperty("isOwner")
    public boolean isOwner() {
        return isOwner;
    }

    @JsonProperty("isOwner")
    public void setOwner(boolean owner){
        isOwner = owner;
    }
}