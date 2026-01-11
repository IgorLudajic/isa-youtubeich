package com.team44.isa_youtubeich.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aot.generate.GeneratedMethod;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "COMMENTS")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String text;

    @Column(name = "created_at")
    @Getter @Setter
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id")
    @Getter @Setter
    private Video video;
}
