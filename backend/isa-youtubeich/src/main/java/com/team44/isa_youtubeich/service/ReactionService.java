package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.ReactionType;

public interface ReactionService {
    void react(Long videoId, ReactionType reactionType, String username);
}
