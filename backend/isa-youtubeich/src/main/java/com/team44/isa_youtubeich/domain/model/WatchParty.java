package com.team44.isa_youtubeich.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "WATCH_PARTIES")
public class WatchParty implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Getter @Setter
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @Getter @Setter
    private User owner;

    @Column(name = "created_at")
    @Getter @Setter
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        if(this.createdAt == null){
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
