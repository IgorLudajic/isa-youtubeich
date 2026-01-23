package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {
}
