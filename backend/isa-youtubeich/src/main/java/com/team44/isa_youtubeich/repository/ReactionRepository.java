package com.team44.isa_youtubeich.repository;


import com.team44.isa_youtubeich.domain.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Reaction findByVideoIdAndUserUsername(Long videoId, String username);
}
