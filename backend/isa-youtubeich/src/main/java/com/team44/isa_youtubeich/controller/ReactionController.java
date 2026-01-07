package com.team44.isa_youtubeich.controller;

import com.team44.isa_youtubeich.domain.model.ReactionType;
import com.team44.isa_youtubeich.service.ReactionService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/video/{videoId}")
    public ResponseEntity<Void> react(@PathVariable Long videoId, @RequestParam ReactionType type, Principal principal){
        reactionService.react(videoId, type, principal.getName());
        return ResponseEntity.ok().build();
    }
}
