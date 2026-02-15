package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.dto.WatchPartyDto;
import com.team44.isa_youtubeich.dto.WatchPartyRequestDto;
import com.team44.isa_youtubeich.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/watch-party")
public class WatchPartyController {

    @Autowired
    private WatchPartyService watchPartyService;

    @GetMapping
    public ResponseEntity<List<WatchPartyDto>> getAll(){
        return ResponseEntity.ok(watchPartyService.getAllParties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchPartyDto> getDetails(@PathVariable Long id, Principal principal){
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(watchPartyService.getPartyDetails(id, username));
    }

    @PostMapping
    public ResponseEntity<WatchPartyDto> create(@RequestBody WatchPartyRequestDto request, Principal principal){
        if(principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(watchPartyService.createParty(request.getName(), principal.getName()));
    }

    @PostMapping("/{id}/play/{videoId}")
    public ResponseEntity<Void> playVideo(@PathVariable Long id, @PathVariable Long videoId, Principal principal){
        if(principal == null) return ResponseEntity.status(401).build();
        watchPartyService.startVideo(id, videoId, principal.getName());
        return ResponseEntity.ok().build();
    }
}
