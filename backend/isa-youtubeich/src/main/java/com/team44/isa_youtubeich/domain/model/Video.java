package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "VIDEOS")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    // TODO likeCount, dislikeCount (???)

    @Column(name = "created_at")
    @Getter @Setter
    private Timestamp createdAt;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "VIDEO_TAGS")
    @Getter @Setter
    private List<String> tags;

    @Column(name = "premieres_at")
    @Getter @Setter
    private Timestamp premieresAt;

    @Embedded
    @Column(name = "file_size")
    @Getter @Setter
    private FileSize fileSize;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @Getter @Setter
    private User user;
}
