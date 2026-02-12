package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "DAILY_VIDEO_POPULARITY")
@Getter @Setter
public class DailyVideoPopularity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_timestamp")
    private Timestamp runTimestamp;

    @ManyToOne
    @JoinColumn(name = "top1_video_id")
    private Video top1Video;

    @Column(name = "top1_score")
    private Double top1Score;

    @ManyToOne
    @JoinColumn(name = "top2_video_id")
    private Video top2Video;

    @Column(name = "top2_score")
    private Double top2Score;

    @ManyToOne
    @JoinColumn(name = "top3_video_id")
    private Video top3Video;

    @Column(name = "top3_score")
    private Double top3Score;
}
