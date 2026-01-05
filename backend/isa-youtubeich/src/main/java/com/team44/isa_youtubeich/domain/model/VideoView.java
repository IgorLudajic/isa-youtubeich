package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "VIDEO_VIEWS")
public class VideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Column(name = "viewed_at")
    @Getter @Setter
    private Timestamp viewedAt;

    @Embedded
    @Getter @Setter
    private GeoLocation viewedFrom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id")
    @Getter @Setter
    private Video video;
}
