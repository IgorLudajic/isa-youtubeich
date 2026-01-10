package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Video> findByUserUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    // JPQL annotations for avoiding race conditions upon updating denormalized counters

    @Modifying
    @Query("UPDATE Video v SET v.likes = v.likes + 1 WHERE v.id = :id")
    void incrementLikes(Long id);

    @Modifying
    @Query("UPDATE Video v SET v.likes = v.likes - 1 WHERE v.id = :id")
    void decrementLikes(Long id);

    @Modifying
    @Query("UPDATE Video v SET v.dislikes = v.dislikes + 1 WHERE v.id = :id")
    void incrementDislikes(Long id);

    @Modifying
    @Query("UPDATE Video v SET v.dislikes = v.dislikes - 1 WHERE v.id = :id")
    void decrementDislikes(Long id);

    @Modifying
    @Query("UPDATE Video v SET v.viewCount = v.viewCount + 1 WHERE v.id = :id")
    void incrementViewCount(Long id);

    @Query("SELECT v FROM Video v WHERE v.premieresAt IS NULL OR v.premieresAt <= :now ORDER BY v.createdAt DESC")
    Page<Video> findAllReleasedVideos(@Param("now") LocalDateTime now, Pageable pageable);
}