package com.team44.isa_youtubeich.repository;

import com.team44.isa_youtubeich.domain.model.DailyVideoPopularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyVideoPopularityRepository extends JpaRepository<DailyVideoPopularity, Long> {

    Optional<DailyVideoPopularity> findTopByOrderByRunTimestampDesc();
}
