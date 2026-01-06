package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    // Ovde možemo dodati metode za pretragu kasnije
}