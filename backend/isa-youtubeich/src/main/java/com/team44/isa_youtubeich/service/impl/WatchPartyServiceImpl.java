package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.WatchParty;
import com.team44.isa_youtubeich.dto.WatchPartyDto;
import com.team44.isa_youtubeich.dto.WatchPartyEventDto;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.repository.WatchPartyRepository;
import com.team44.isa_youtubeich.service.WatchPartyRedisService;
import com.team44.isa_youtubeich.service.WatchPartyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchPartyServiceImpl implements WatchPartyService {

    @Autowired
    private WatchPartyRepository watchPartyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private WatchPartyRedisService watchPartyRedisService;


    @Override
    @Transactional(readOnly = true)
    public List<WatchPartyDto> getAllParties() {
        return watchPartyRepository.findAllByOrderByCreatedAtDesc().stream().map(wp -> new WatchPartyDto(
                wp.getId(), wp.getName(), wp.getOwner().getUsername(), false
        )).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WatchPartyDto getPartyDetails(Long id, String currentUsername) {
        WatchParty wp = watchPartyRepository.findById(id).orElseThrow(() -> new RuntimeException("Watch party not found"));

        boolean isOwner = currentUsername != null && currentUsername.equals(wp.getOwner().getUsername());

        return new WatchPartyDto(wp.getId(), wp.getName(), wp.getOwner().getUsername(), isOwner);
    }

    @Override
    @Transactional
    public WatchPartyDto createParty(String name, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername);
        if(owner == null) throw new RuntimeException("User not found");

        WatchParty wp = new WatchParty();
        wp.setName(name);
        wp.setOwner(owner);

        wp = watchPartyRepository.save(wp);
        return new WatchPartyDto(wp.getId(), wp.getName(), wp.getOwner().getUsername(), true);
    }

    @Override
    public void startVideo(Long partyId, Long videoId, String username) {
        WatchParty wp = watchPartyRepository.findById(partyId).orElseThrow(() -> new RuntimeException("Party not found"));

        if(!wp.getOwner().getUsername().equals(username)){
            throw new RuntimeException("Only the owner can start a video");
        }

        if(!videoRepository.existsById(videoId)){
            throw new RuntimeException("Video does not exist");
        }

        WatchPartyEventDto event = new WatchPartyEventDto("REDIRECT", videoId, partyId);
        watchPartyRedisService.broadcastToCluster(event);
    }
}
