package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Video> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
}