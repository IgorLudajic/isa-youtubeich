package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.dto.WatchPartyDto;

import java.util.List;

public interface WatchPartyService {
    List<WatchPartyDto> getAllParties();
    WatchPartyDto getPartyDetails(Long id, String currentUsername);
    WatchPartyDto createParty(String name, String ownerUsername);
    void startVideo(Long partyId, Long videoId, String username);
}
