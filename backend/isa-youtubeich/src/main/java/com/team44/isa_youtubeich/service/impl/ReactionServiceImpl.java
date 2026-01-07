package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Reaction;
import com.team44.isa_youtubeich.domain.model.ReactionType;
import com.team44.isa_youtubeich.domain.model.User;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.exception.ResourceConflictException;
import com.team44.isa_youtubeich.repository.ReactionRepository;
import com.team44.isa_youtubeich.repository.UserRepository;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.ReactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void react(Long videoId, ReactionType type, String username){
        User user = userRepository.findByUsername(username);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new ResourceConflictException(videoId, "Video not found"));

        Reaction existing = reactionRepository.findByVideoIdAndUserUsername(videoId, username);

        if(existing != null){
            if(existing.getType() == type){
                reactionRepository.delete(existing);
            }
            else {
                existing.setType(type);
                reactionRepository.save(existing);
            }
        }
        else {
            Reaction newReaction = new Reaction();
            newReaction.setUser(user);
            newReaction.setVideo(video);
            newReaction.setType(type);
            newReaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            reactionRepository.save(newReaction);
        }
    }
}
