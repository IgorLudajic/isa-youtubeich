package com.team44.isa_youtubeich.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "REACTIONS")
public class Reaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    private ReactionType type;

    @Column(name = "created_at")
    @Getter @Setter
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "video_id")
    @Getter @Setter
    private Video video;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User user;
}
