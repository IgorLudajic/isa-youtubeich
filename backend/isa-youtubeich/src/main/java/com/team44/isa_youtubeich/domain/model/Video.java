package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VIDEOS")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Deprecated(forRemoval = true)
    @Getter @Setter
    private Long likes;

    @Deprecated(forRemoval = true)
    @Getter @Setter
    private Long dislikes;

    @Column(name = "created_at")
    @Getter @Setter
    private Timestamp createdAt;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String description;

    @Deprecated(forRemoval = true)
    @Column(columnDefinition = "bigint default 0")
    @Getter @Setter
    private Long viewCount = 0L;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "VIDEO_TAGS")
    @Getter @Setter
    private List<String> tags;

    @Column(name = "premieres_at")
    @Getter @Setter
    private LocalDateTime premieresAt;

    @Embedded
    @Column(name = "file_size")
    @Getter @Setter
    private FileSize fileSize;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @Getter @Setter
    private User user;

    @Column(name = "video_url")
    @Getter @Setter
    private String videoUrl;

    @Column(name = "thumbnail_url")
    @Getter @Setter
    private String thumbnailUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude"))
    })
    @Getter @Setter
    private GeoLocation location;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
