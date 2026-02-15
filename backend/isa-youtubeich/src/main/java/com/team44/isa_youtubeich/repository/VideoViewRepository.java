package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    long countByVideo(@Param("video") Video video);

    List<VideoView> findByViewedAtGreaterThanEqual(Timestamp timestamp);
}
