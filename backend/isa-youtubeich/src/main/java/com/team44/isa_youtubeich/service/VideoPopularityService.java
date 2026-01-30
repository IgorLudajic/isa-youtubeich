package com.team44.isa_youtubeich.service;

import com.team44.isa_youtubeich.domain.model.DailyVideoPopularity;
import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoView;
import com.team44.isa_youtubeich.repository.DailyVideoPopularityRepository;
import com.team44.isa_youtubeich.repository.VideoViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoPopularityService {

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private DailyVideoPopularityRepository dailyVideoPopularityRepository;

    public void calculateAndSavePopularity() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp since = Timestamp.valueOf(now.minusHours(24));

        List<VideoView> views = videoViewRepository.findByViewedAtGreaterThanEqual(since);

        Map<Video, Double> scoreMap = new HashMap<>();
        for (VideoView view : views) {
            long days = ChronoUnit.DAYS.between(view.getViewedAt().toLocalDateTime(), now);
            double score = 7 - days + 1;
            scoreMap.merge(view.getVideo(), score, Double::sum);
        }

        List<Map.Entry<Video, Double>> sorted = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Video, Double>comparingByValue().reversed())
                .limit(3)
                .toList();

        DailyVideoPopularity popularity = new DailyVideoPopularity();
        popularity.setRunTimestamp(Timestamp.valueOf(now));

        if (!sorted.isEmpty()) {
            popularity.setTop1Video(sorted.getFirst().getKey());
            popularity.setTop1Score(sorted.getFirst().getValue());
        }
        if (sorted.size() > 1) {
            popularity.setTop2Video(sorted.get(1).getKey());
            popularity.setTop2Score(sorted.get(1).getValue());
        }
        if (sorted.size() > 2) {
            popularity.setTop3Video(sorted.get(2).getKey());
            popularity.setTop3Score(sorted.get(2).getValue());
        }

        dailyVideoPopularityRepository.save(popularity);
    }
}
