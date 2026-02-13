package com.team44.isa_youtubeich.service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoPopularityScheduler {

    @Autowired
    private VideoPopularityService videoPopularityService;

    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(name = "videoPopularityEtl", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void runEtl() {
        videoPopularityService.calculateAndSavePopularity();
    }
}
